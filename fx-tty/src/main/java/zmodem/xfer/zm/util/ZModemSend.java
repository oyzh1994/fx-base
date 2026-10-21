package zmodem.xfer.zm.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.IOUtil;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamListener;
import zmodem.FileCopyStreamEvent;
import zmodem.util.FileAdapter;
import zmodem.xfer.util.InvalidChecksumException;
import zmodem.xfer.zm.packet.Cancel;
import zmodem.xfer.zm.packet.DataPacket;
import zmodem.xfer.zm.packet.Finish;
import zmodem.xfer.zm.packet.Format;
import zmodem.xfer.zm.packet.Header;
import zmodem.zm.io.ZMPacketInputStream;
import zmodem.zm.io.ZMPacketOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;


public class ZModemSend {

    private static final int packLen = 1024 * 8;

    private final byte[] data = new byte[packLen];
    private final CopyStreamAdapter adapter = new CopyStreamAdapter();
    private final Supplier<List<FileAdapter>> destinationSupplier;
    private final InputStream netIs;
    private final OutputStream netOs;

    private List<FileAdapter> files;
    private Iterator<FileAdapter> iter;

    private FileAdapter file;
    private long fOffset = 0;
    private int index = 0;
    private long filesize = 0;
    private boolean atEof = false;
    private InputStream fileIs;


    public ZModemSend(Supplier<List<FileAdapter>> destinationSupplier, InputStream netin, OutputStream netout) throws IOException {
        this.destinationSupplier = destinationSupplier;
        netIs = netin;
        netOs = netout;
    }

    public boolean nextFile() throws IOException {
        closeFile();

        if (files == null) {
            files = destinationSupplier.get();
            if (files == null) {
                return false;
            }
            iter = files.iterator();
        }

        if (!iter.hasNext())
            return false;


        file = iter.next();
        fileIs = file.getInputStream();
        filesize = file.length();
        if (filesize > 0xffffffffL) {
            throw new IOException("ZModem does not support files larger than 4GB");
        }
        fOffset = 0;
        atEof = false;
        index++;

        return true;
    }

    /**
     * 关闭当前文件流并清空引用，避免后续重传或确认包继续读取已关闭的流。
     */
    private void closeFile() {
        IOUtil.closeQuietly(fileIs);
        fileIs = null;
    }


    public void addCopyStreamListener(CopyStreamListener listener) {
        adapter.addCopyStreamListener(listener);
    }

    public void removeCopyStreamListener(CopyStreamListener listener) {
        adapter.removeCopyStreamListener(listener);
    }


    private void position(long offset) throws IOException {
        if (offset < 0 || offset > this.filesize) {
            throw new IOException("Invalid ZModem resume position: " + offset);
        }
        if (file == null) {
            throw new IOException("ZModem send file is not open");
        }
        if (fileIs != null && !atEof && offset >= fOffset) {
            fileIs.skipNBytes(offset - fOffset);
        } else {
            // ZRPOS是绝对位置。文件流不能回退，重传时必须重新打开文件并从起点跳过。
            closeFile();
            fileIs = file.getInputStream();
            fileIs.skipNBytes(offset);
        }
        fOffset = offset;
    }

    private byte[] getNextBlock() throws IOException {
        // 防御性恢复：EOF 后流已置空，但状态机仍可能收到重传请求。
        if (fileIs == null) {
            if (file == null) {
                throw new IOException("ZModem send file is not open");
            }
            fileIs = file.getInputStream();
            fileIs.skipNBytes(fOffset);
        }
        final int len = fileIs.readNBytes(data, 0, data.length);

        if (len == 0) {
            atEof = true;
            return null;
        }

        atEof = len < data.length;
        fOffset += len;

        if (len != data.length)
            return ArrayUtil.subarray(data, 0, len);
        else
            return data;
    }

    private DataPacket getNextDataPacket() throws IOException {
        byte[] data = getNextBlock();

        ZModemCharacter fe = ZModemCharacter.ZCRCW;
        if (atEof) {
            fe = ZModemCharacter.ZCRCE;
            closeFile();
        }

        if (data == null) {
            return new DataPacket(fe);
        }

        return new DataPacket(fe, data);
    }

    private void sendNextData(ZMPacketOutputStream os) throws IOException {
        os.write(new Header(Format.BIN, ZModemCharacter.ZDATA, (int) fOffset));
        os.write(getNextDataPacket());
        if (atEof) {
            os.write(new Header(Format.HEX, ZModemCharacter.ZEOF, (int) fOffset));
        }
        fireBytesTransferred(false);
    }

    /**
     * 发送下一个文件的信息；没有更多文件时发送 ZFIN。
     */
    private void sendNextFileOrFinish(ZMPacketOutputStream os, ZMPacketFactory factory) throws IOException {
        if (!nextFile()) {
            os.write(new Header(Format.BIN, ZModemCharacter.ZFIN));
            return;
        }
        sendFileInfo(os, factory);
    }

    /**
     * 发送当前文件的信息，不改变文件读取位置。
     */
    private void sendFileInfo(ZMPacketOutputStream os, ZMPacketFactory factory) throws IOException {
        os.write(new Header(Format.BIN, ZModemCharacter.ZFILE, new byte[]{0, 0, 0, ZMOptions.with(ZMOptions.ZCBIN)}));
        os.write(factory.createZFilePacket(file.getName(), filesize, 0, "0",
                files.size() - index, 0));
        fireBytesTransferred(false);
    }

    public void send(Supplier<Boolean> isCancelled) throws IOException {
        ZMPacketFactory factory = new ZMPacketFactory();

        ZMPacketInputStream is = new ZMPacketInputStream(netIs);
        ZMPacketOutputStream os = new ZMPacketOutputStream(netOs);
        boolean cancelled = false;
        try {

            boolean end = false;
            int errorCount = 0;
            ZMPacket packet = null;

            while (!end) {
                if (isCancelled.get()) {
                    if (!cancelled) {
                        os.write(new Cancel());
                        fireBytesTransferred(true);
                        cancelled = true;
                    }
                    break;
                }
                packet = null;
                try {
                    packet = is.read();
                    errorCount = 0;
                } catch (InvalidChecksumException ice) {
                    ++errorCount;
                    if (errorCount > 20) {
                        os.write(new Cancel());
                        end = true;
                    }
                    continue;
                }

                if (packet instanceof Cancel) {
                    end = true;
                } else if (isCancelled.get()) {
                    if (!cancelled) {
                        os.write(new Cancel());
                        fireBytesTransferred(true);
                        cancelled = true;
                    }
                    break;
                }

                if (!end && packet instanceof Header header) {
                    switch (header.type()) {
                        case ZSKIP:
                            fireBytesTransferred(true);
                            closeFile();
                            atEof = true;
                            sendNextFileOrFinish(os, factory);
                            break;
                        case ZRINIT:
                            // 重复的ZRINIT只表示远端仍在等待文件信息，不能跳过当前文件。
                            // 原实现每次ZRINIT都会调用nextFile()，在单文件传输中会提前关闭
                            // 文件流，随后ZACK继续发送时就会读取已关闭的流。
                            if (fileIs != null && !atEof) {
                                sendFileInfo(os, factory);
                            } else {
                                sendNextFileOrFinish(os, factory);
                            }
                            break;
                        case ZRPOS:
                            position(Integer.toUnsignedLong(header.getPos()));
                            atEof = false;
                            sendNextData(os);
                            break;
                        case ZACK:
                            // 文件已到EOF且ZEOF已发送，忽略远端可能重复发送的确认包。
                            if (fileIs != null && !atEof) {
                                sendNextData(os);
                            }
                            break;
                        case ZFIN:
                            end = true;
                            os.write(new Finish());
                            break;
                        default:
                            end = true;
                            os.write(new Cancel());
                            break;
                    }

                }
            }
        } catch (IOException | RuntimeException e) {
            boolean cancelRequested = cancelled || isCancelled.get();
            if (!cancelRequested && JulLog.isErrorEnabled()) {
                JulLog.error("ZModem发送失败", e);
            }
            if (!cancelRequested) {
                try {
                    os.write(new Cancel());
                } catch (IOException ignore) {
                    // 连接已断开时无需再次处理
                }
            }
            throw e;
        } finally {
            closeFile();
        }

    }

    private void fireBytesTransferred(boolean skip) {
        if (this.file == null || this.files == null) {
            return;
        }
        adapter.bytesTransferred(new FileCopyStreamEvent(this, file.getName(), files.size() - index, index,
                this.filesize, (int) Math.min(this.fOffset, Integer.MAX_VALUE), 0, skip));
    }
}
