package cn.oyzh.fx.common.thread;

/**
 * @author oyzh
 * @since 2024/6/7
 */
public class ThreadExt extends Thread {

    private Boolean finish;

    public ThreadExt() {
        super();
    }

    public ThreadExt(Runnable task) {
        super(task);
    }

    @Override
    public void interrupt() {
        this.finish = true;
        super.interrupt();
    }

    @Override
    public boolean isInterrupted() {
        return (this.finish != null && this.finish) || super.isInterrupted();
    }
}
