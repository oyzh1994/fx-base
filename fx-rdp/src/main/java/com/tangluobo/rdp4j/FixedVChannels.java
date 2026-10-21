package com.tangluobo.rdp4j;

import cn.oyzh.common.log.JulLog;
import com.tangluobo.rdp4j.rdp5.VChannel;
import com.tangluobo.rdp4j.rdp5.VChannels;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 修复版虚拟通道集合（VChannels）。
 *
 * javardp 3.0.0 的{@link VChannels#channel_process}在处理多分片虚拟通道消息时
 * 存在致命bug：分片数据通过Utilities.concatenateBytes({fragment_buffer, data})
 * 拼接，而fragment_buffer初始为null（且每次重组完成后被置回null），首次分片
 * 即对null元素读取length抛NullPointerException（"cannot read the array length
 * because 'b' is null"），异常沿RDP接收线程上传导致连接断开。
 *
 * 库内通道（cliprdr文本、DisplayControl）消息都很小，永远单分片
 * （CHANNEL_FLAG_FIRST|CHANNEL_FLAG_LAST同时置位直接process），从未触发过
 * 分片路径，因此bug一直未暴露。文件剪贴板的大消息（FILEGROUPDESW描述列表、
 * FILECONTENTS_RESPONSE数据块）超过单个MCS PDU（约1590字节）必然分片，一触即崩。
 *
 * 本类覆盖channel_process，修复点：
 * 1. 分片缓冲null安全拼接（System.arraycopy手工合并）；
 * 2. 每个MCS通道独立保存重组状态，允许不同虚拟通道的分片交错到达；
 * 3. 收到CHANNEL_FLAG_FIRST分片时重置缓冲，防止丢包/乱序后拼入残留数据；
 * 4. 拒绝无FIRST的孤立中间分片，并限制单条重组消息大小，避免远端触发OOM。
 */
public class FixedVChannels extends VChannels {

    private static final Logger logger = Logger.getLogger(FixedVChannels.class.getName());

    /**
     * 单条虚拟通道消息允许重组的最大长度。正常CLIPRDR文件内容块由客户端
     * 主动按1MB请求，64MB远高于正常单条消息；设置上限用于阻止恶意或损坏
     * 的服务器通过无限FIRST/中间分片把接收端堆内存耗尽。
     */
    private static final int MAX_REASSEMBLED_MESSAGE_BYTES = 64 * 1024 * 1024;

    /** 每个MCS通道各自的分片重组缓冲，避免不同通道交错时互相覆盖。 */
    private final Map<Integer, byte[]> fragmentBuffers = new HashMap<>();
    /** 各通道收到的数据包计数（诊断：确认音频通道是否有数据到达） */
    private final Map<Integer, Integer> channelPacketCounts = new java.util.concurrent.ConcurrentHashMap<>();

    public FixedVChannels(State state) {
        super(state);
    }

    @Override
    public void channel_process(Packet packet, int mcsChannel) throws RdesktopException, IOException {
        VChannel channel = null;
        for (int i = 0; i < num_channels(); i++) {
            if (mcs_id(i) == mcsChannel) {
                channel = channel(i);
                break;
            }
        }
        if (channel == null) {
            JulLog.warn("Data from unknown channel " + mcsChannel);
            return;
        }

        // 诊断：记录各通道数据包计数。文件剪贴板会把大文件拆成数万
        // 个约1.6KB分片，降低它的采样频率，避免同步日志拖慢下载。
        int count = channelPacketCounts.merge(mcsChannel, 1, Integer::sum);
        int logInterval = "cliprdr".equals(channel.name()) ? 5000 : 100;
        if (count <= 2 || count % logInterval == 0) {
            JulLog.info(String.format("[VCHAN] 通道%s(%s) 收到第%d个数据包, %d字节",
                    mcsChannel, channel.name(), count, packet.getEnd() - packet.getPosition()));
        }

        // 虚拟通道chunk头：length(4) + flags(4)
        if (packet.getEnd() - packet.getPosition() < 8) {
            JulLog.warn("虚拟通道" + mcsChannel + "的chunk头被截断");
            return;
        }
        int declaredLength = packet.getLittleEndian32();
        int flags = packet.getLittleEndian32();
        long declaredUnsignedLength = Integer.toUnsignedLong(declaredLength);
        if (declaredUnsignedLength > MAX_REASSEMBLED_MESSAGE_BYTES) {
            fragmentBuffers.remove(mcsChannel);
            JulLog.warn("虚拟通道" + mcsChannel + "声明了过大的消息: " + declaredUnsignedLength);
            return;
        }
        // 诊断：记录到达的通道数据，确认服务器是否在给rdpsnd通道推送数据
        logger.finest("channel_process: mcs=" + mcsChannel + " channel=" + channel.name()
                + " flags=0x" + Integer.toHexString(flags) + " remain="
                + (packet.getEnd() - packet.getPosition()));

        if ((flags & CHANNEL_FLAG_FIRST) != 0 && (flags & CHANNEL_FLAG_LAST) != 0) {
            // 单分片完整消息：直接处理（position已跳过chunk头）
            fragmentBuffers.remove(mcsChannel);
            channel.process(packet);
            return;
        }

        // 多分片消息：取出当前chunk数据（position到end）
        if ((flags & CHANNEL_FLAG_FIRST) != 0) {
            // 新消息的第一个分片：重置缓冲（防上次残留）
            fragmentBuffers.put(mcsChannel, new byte[0]);
        } else if (!fragmentBuffers.containsKey(mcsChannel)) {
            // 中间/最后分片必须依附于同通道的FIRST分片。旧实现会把这种
            // 孤立分片当成新消息起点，随后把损坏数据交给上层解析。
            JulLog.warn("虚拟通道" + mcsChannel + "收到无FIRST的孤立分片, flags=0x"
                    + Integer.toHexString(flags));
            return;
        }
        int avail = packet.getEnd() - packet.getPosition();
        byte[] data = new byte[avail];
        if (avail > 0) {
            packet.copyToByteArray(data, 0, packet.getPosition(), avail);
        }

        byte[] fragmentBuffer = fragmentBuffers.get(mcsChannel);
        long mergedLength = (long) fragmentBuffer.length + data.length;
        if (mergedLength > MAX_REASSEMBLED_MESSAGE_BYTES) {
            fragmentBuffers.remove(mcsChannel);
            JulLog.warn("虚拟通道" + mcsChannel + "的重组消息超过上限: " + mergedLength);
            return;
        }
        byte[] merged = new byte[(int) mergedLength];
        System.arraycopy(fragmentBuffer, 0, merged, 0, fragmentBuffer.length);
        System.arraycopy(data, 0, merged, fragmentBuffer.length, data.length);
        fragmentBuffers.put(mcsChannel, merged);

        if ((flags & CHANNEL_FLAG_LAST) != 0) {
            // 最后一个分片：重组完整消息并处理
            // Packet(int)构造后position=0，copyFromByteArray不推进position，
            // process()内从position 0读消息头，与单分片路径一致
            fragmentBuffers.remove(mcsChannel);
            Packet whole = new Packet(merged.length);
            whole.copyFromByteArray(merged, 0, 0, merged.length);
            channel.process(whole);
        }
    }
}
