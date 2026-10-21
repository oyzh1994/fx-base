package zmodem.xfer.zm.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamListener;
import zmodem.FileCopyStreamEvent;
import zmodem.util.EmptyFileAdapter;
import zmodem.util.FileAdapter;
import zmodem.xfer.util.InvalidChecksumException;
import zmodem.xfer.zm.packet.Cancel;
import zmodem.xfer.zm.packet.DataPacket;
import zmodem.xfer.zm.packet.Finish;
import zmodem.xfer.zm.packet.Format;
import zmodem.xfer.zm.packet.Header;
import zmodem.zm.io.ZMPacketInputStream;
import zmodem.zm.io.ZMPacketOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;


public class ZModemReceive {

    private final CopyStreamAdapter adapter = new CopyStreamAdapter();
    private final Supplier<FileAdapter> destinationSupplier;
    private FileAdapter destination;

    private FileAdapter file;
    private int fOffset = 0;
    private Long filesize;
    private int remaining = 0;
    private int index = 0;

    private OutputStream fileOs = null;

    private final InputStream netIs;
    private final OutputStream netOs;

    private enum Expect {
        FILENAME, DATA, NOTHING;
    }

    public ZModemReceive(Supplier<FileAdapter> destDir, InputStream netin, OutputStream netout) throws IOException {
        destinationSupplier = destDir;
        netIs = netin;
        netOs = netout;
    }

    private void open(int offset) throws IOException {
        if (file == null) {
            throw new IOException("Received data before ZModem file name");
        }
        boolean append = false;

        if (offset != 0) {
            if (file.exists() && file.length() == offset)
                append = true;
            else
                offset = 0;
        }

        IOUtil.closeQuietly(fileOs);

        fileOs = file.getOutputStream(append);
        fOffset = offset;

    }

    private void decodeFileNameData(DataPacket p) throws IOException {
        ByteArrayOutputStream filename = new ByteArrayOutputStream();
        StringBuilder extract = new StringBuilder();
        byte[] data = p.data();
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            if (b == 0) {
                for (int j = i + 1; j < data.length; j++) {
                    b = data[j];
                    if (b == 0) {
                        break;
                    }
                    extract.append((char) b);
                }
                break;
            }
            filename.write(b);
        }

        String safeName = new File(filename.toString().replace('\\', '/')).getName();
        if (safeName.isEmpty() || ".".equals(safeName) || "..".equals(safeName)) {
            throw new IOException("Invalid ZModem file name: " + filename);
        }

        final String[] segments = extract.toString().split(StringUtil.SPACE);
        if (ArrayUtil.isEmpty(segments)) {
            throw new IOException("Invalid ZModem file metadata");
        }
        // filesize
        this.filesize = NumberUtil.toLong(segments[0]);
        if (this.filesize == null || this.filesize < 0) {
            throw new IOException("Invalid ZModem file size: " + segments[0]);
        }
        // remaining
        if (segments.length >= 5) {
            this.remaining = NumberUtil.toInt(segments[4]);
        }

        file = destination.getChild(safeName);
        if (file == null) {
            throw new IOException("Invalid ZModem file name: " + safeName);
        }
        fOffset = 0;

        index++;

        adapter.bytesTransferred(new FileCopyStreamEvent(this, file.getName(), remaining, index,
                this.filesize, fOffset, 0, false));
    }

    public void addCopyStreamListener(CopyStreamListener listener) {
        adapter.addCopyStreamListener(listener);
    }

    public void removeCopyStreamListener(CopyStreamListener listener) {
        adapter.removeCopyStreamListener(listener);
    }

    private void writeData(DataPacket p) throws IOException {
        if (this.fileOs == null) {
            throw new IOException("Received data before ZModem file was opened");
        }
        final byte[] data = p.data();

        fileOs.write(data);
        fOffset += data.length;

        // 开始传输
        adapter.bytesTransferred(new FileCopyStreamEvent(this, file.getName(), remaining, index,
                this.filesize, fOffset, 0, false));
    }

    private boolean initDestination() {
        if (destination != null) {
            return true;
        }
        destination = destinationSupplier.get();
        return !(destination instanceof EmptyFileAdapter);
    }

    public void receive(Supplier<Boolean> isCancelled) throws IOException {
        ZMPacketInputStream is = new ZMPacketInputStream(netIs);
        ZMPacketOutputStream os = new ZMPacketOutputStream(netOs);

        Expect expect = Expect.NOTHING;

        byte[] recvOpt = {0, 4, 0, ZMOptions.with(ZMOptions.ESCCTL, ZMOptions.ESC8)};

        try {

            boolean end = false;
            int errorCount = 0;
            ZMPacket packet = null;
            while (!end) {
                if (isCancelled.get()) {
                    break;
                }
                packet = null;
                try {
                    packet = is.read();
                    errorCount = 0;
                } catch (InvalidChecksumException ice) {
                    if (JulLog.isErrorEnabled()) {
                        JulLog.error("ZModem包校验失败", ice);
                    }
                    ++errorCount;
                    if (errorCount >= 3) {
                        os.write(new Cancel());
                        end = true;
                    }
                    continue;
                }

                if (packet instanceof Cancel) {
                    end = true;
                } else if (packet instanceof Finish) {
                    end = true;
                }

                if (isCancelled.get()) {
                    break;
                }

                // 如果重定向为空，则终止传输
                if (destination instanceof EmptyFileAdapter) {
                    os.write(new Cancel());
                    break;
                }

                if (packet instanceof Header header) {
                    switch (header.type()) {
                        case ZRQINIT:
                            os.write(new Header(Format.HEX, ZModemCharacter.ZRINIT, recvOpt));
                            break;
                        case ZFILE:
                            expect = Expect.FILENAME;
                            break;
                        case ZEOF:
                            os.write(new Header(Format.HEX, ZModemCharacter.ZRINIT, recvOpt));
                            expect = Expect.NOTHING;
                            file = null;
                            if (fileOs != null) {
                                fileOs.flush();
                                IOUtil.closeQuietly(fileOs);
                            }
                            fileOs = null;
                            break;
                        case ZDATA:
                            open(header.getPos());
                            expect = Expect.DATA;
                            break;
                        case ZFIN:
                            os.write(new Header(Format.HEX, ZModemCharacter.ZFIN));
                            end = true;
                            break;
                        default:
                            end = true;
                            os.write(new Cancel());
                            break;
                    }
                }

                if (packet instanceof DataPacket data) {
                    switch (expect) {
                        case NOTHING:
                            os.write(new Header(Format.HEX, ZModemCharacter.ZRINIT, recvOpt));
                            break;
                        case FILENAME:
                            if (!initDestination()) {
                                end = true;
                                os.write(new Cancel());
                                break;
                            }
                            decodeFileNameData(data);
                            if (file.exists() && file.length() == filesize) {
                                os.write(new Header(Format.HEX, ZModemCharacter.ZSKIP));
                                adapter.bytesTransferred(new FileCopyStreamEvent(this, file.getName(), remaining, index,
                                        this.filesize, fOffset, 0, true));
                            } else {
                                os.write(new Header(Format.HEX, ZModemCharacter.ZRPOS, (int) file.length()));
                            }
                            expect = Expect.NOTHING;
                            break;
                        case DATA:
                            writeData(data);
                            switch (data.type()) {
                                case ZCRCW:
                                    expect = Expect.NOTHING;
                                case ZCRCQ:
                                    os.write(new Header(Format.HEX, ZModemCharacter.ZACK, fOffset));
                                    break;
                                case ZCRCE:
                                    expect = Expect.NOTHING;
                                    break;
                            }
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            if (isCancelled.get()) {
                // 取消后远端可能直接断开或只发送残留字节，不再回复 CAN。
                throw e;
            }
            if (JulLog.isErrorEnabled()) {
                JulLog.error("ZModem接收失败", e);
            }
            try {
                os.write(new Cancel());
            } catch (IOException ignore) {
                // 连接已断开时无需再次处理
            }
            throw e;
        } finally {
            IOUtil.closeQuietly(fileOs);
        }

    }
}
