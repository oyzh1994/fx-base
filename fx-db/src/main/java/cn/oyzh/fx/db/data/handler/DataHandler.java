package cn.oyzh.fx.db.data.handler;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 数据处理基类，提供中断控制、消息通知与进度处理等通用能力
 *
 * @author oyzh
 * @since 2026-09-01
 */
public class DataHandler {

    /**
     * 中断标志位
     */
    protected AtomicBoolean interrupt;

    /**
     * 消息处理
     */
    private Consumer<String> messageHandler;

    /**
     * 进度处理
     */
    private Consumer<Integer> processedHandler;

    /**
     * 设置中断
     *
     * @param interrupt 中断标志位
     */
    public void interrupt(boolean interrupt) {
        if (this.interrupt == null) {
            this.interrupt = new AtomicBoolean(interrupt);
        } else {
            this.interrupt.set(interrupt);
        }
    }

    /**
     * 中断
     */
    public void interrupt() {
        this.interrupt(true);
    }

    /**
     * 检查中断，如果中断则抛出中断异常
     *
     * @throws InterruptedException 中断异常
     */
    protected void checkInterrupt() throws InterruptedException {
        if (this.interrupt != null && this.interrupt.get()) {
            throw new InterruptedException();
        }
    }

    /**
     * 发送异常
     *
     * @param ex 异常
     */
    protected void exception(Exception ex) throws Exception {
        if (ex instanceof InterruptedException) {
            throw ex;
        }
        if (this.messageHandler != null) {
            this.messageHandler.accept(ex.getMessage());
        } else {
            ex.printStackTrace();
        }
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    protected void message(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.accept(message);
        }
    }

    /**
     * 更新进度
     *
     * @param processed 进度
     */
    protected void processed(int processed) {
        if (this.processedHandler != null) {
            this.processedHandler.accept(processed);
        }
    }

    /**
     * 忽略进度
     */
    protected void processedSkip() {
        this.processedSkip(0);
    }

    /**
     * 忽略进度，当跳过值不小于 0 时更新进度
     *
     * @param skip 跳过值
     */
    protected void processedSkip(int skip) {
        if (skip >= 0) {
            this.processed(skip);
        }
    }

    /**
     * 递增进度
     */
    protected void processedIncr() {
        this.processedIncr(1);
    }

    /**
     * 递增进度
     *
     * @param incr 递增数量，取绝对值
     */
    protected void processedIncr(int incr) {
        if (incr < 0) {
            incr = Math.abs(incr);
        }
        this.processed(incr);
    }

    /**
     * 递减进度
     */
    protected void processedDecr() {
        this.processedDecr(-1);
    }

    /**
     * 递减进度
     *
     * @param decr 递减数量
     */
    protected void processedDecr(int decr) {
        if (decr > 0) {
            this.processed(-decr);
        } else {
            this.processed(decr);
        }
    }

    /**
     * 获取中断。
     *
     * @return 中断
     */
    public AtomicBoolean getInterrupt() {
        return interrupt;
    }

    /**
     * 设置中断。
     *
     * @param interrupt 中断
     */
    public void setInterrupt(AtomicBoolean interrupt) {
        this.interrupt = interrupt;
    }

    /**
     * 获取消息处理器。
     *
     * @return 消息处理器
     */
    public Consumer<String> getMessageHandler() {
        return messageHandler;
    }

    /**
     * 设置消息处理器。
     *
     * @param messageHandler 消息处理器
     * @return 消息处理器
     */
    public DataHandler setMessageHandler(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    /**
     * 获取处理完成处理器。
     *
     * @return 处理完成处理器
     */
    public Consumer<Integer> getProcessedHandler() {
        return processedHandler;
    }

    /**
     * 设置处理完成处理器。
     *
     * @param processedHandler 处理完成处理器
     */
    public void setProcessedHandler(Consumer<Integer> processedHandler) {
        this.processedHandler = processedHandler;
    }

}
