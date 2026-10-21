package zmodem;


import org.apache.commons.net.io.CopyStreamListener;
import zmodem.util.FileAdapter;
import zmodem.xfer.zm.util.ZModemReceive;
import zmodem.xfer.zm.util.ZModemSend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


public class ZModem {

    private static final byte[] CANCEL_SEQUENCE = createCancelSequence();

    private final InputStream netIs;
    private final OutputStream netOs;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    public ZModem(InputStream in, OutputStream out) {
        this.netIs = in;
        this.netOs = out;
    }

    public void receive(Supplier<FileAdapter> dstDir, CopyStreamListener listener) throws IOException {
        ZModemReceive sender = new ZModemReceive(dstDir, this.netIs, this.netOs);
        sender.addCopyStreamListener(listener);
        sender.receive(this.isCancelled::get);
        if (!this.isCancelled.get()) {
            this.writeLineEnding();
        }
    }

    public void send(Supplier<List<FileAdapter>> filesSupplier, CopyStreamListener listener) throws Exception {
        ZModemSend sender = new ZModemSend(filesSupplier, this.netIs, this.netOs);
        sender.addCopyStreamListener(listener);
        sender.send(this.isCancelled::get);
        if (!this.isCancelled.get()) {
            this.writeLineEnding();
        }
    }

    public void cancel() throws IOException {
        if (this.isCancelled.compareAndSet(false, true)) {
            // 取消可能由UI线程触发，必须与正在发送的协议包串行写入，避免CAN序列插入包中间。
            synchronized (this.netOs) {
                this.netOs.write(CANCEL_SEQUENCE);
                this.netOs.flush();
            }
        }
    }

    public boolean isCancelled() {
        return this.isCancelled.get();
    }

    private void writeLineEnding() throws IOException {
        synchronized (this.netOs) {
            this.netOs.write('\r');
            this.netOs.flush();
        }
    }

    private static byte[] createCancelSequence() {
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            bytes[i] = 0x18;
            bytes[i + 8] = 0x08;
        }
        return bytes;
    }
}
