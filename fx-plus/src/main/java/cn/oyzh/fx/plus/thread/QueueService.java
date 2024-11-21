package cn.oyzh.fx.plus.thread;

import cn.oyzh.fx.plus.util.FXUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TreeView渲染服务
 *
 * @author oyzh
 * @since 2024/03/26
 */
public class QueueService {

    /**
     * 任务队列
     */
    private final Queue<QueueTask> tasks = new ConcurrentLinkedQueue<>();

    /**
     * 渲染中标志位
     */
    private final AtomicBoolean rendering = new AtomicBoolean(false);

    /**
     * 提交任务
     *
     * @param task 任务
     */
    public void submit(Runnable task) {
        if (task != null) {
            this.tasks.add(new QueueTask(task, (byte) 0));
            this.doRender();
        }
    }

    /**
     * 提交任务，fx线程
     *
     * @param task 任务
     */
    public void submitFX(Runnable task) {
        if (task != null) {
            this.tasks.add(new QueueTask(task, (byte) 1));
            this.doRender();
        }
    }

    /**
     * 提交任务，fx线程，并延后处理
     *
     * @param task 任务
     */
    public void submitFXLater(Runnable task) {
        if (task != null) {
            this.tasks.add(new QueueTask(task, (byte) 2));
            this.doRender();
        }
    }

    /**
     * 执行渲染
     */
    protected void doRender() {
        if (!this.rendering.get()) {
            this.rendering.set(true);
            try {
                while (!this.tasks.isEmpty()) {
                    QueueTask task = this.tasks.poll();
                    if (task != null) {
                        if (task.getType() == 2) {
                            FXUtil.runLater(task.getTask());
                        } else if (task.getType() == 1) {
                            FXUtil.runWait(task.getTask());
                        } else {
                            BackgroundService.submit(task.getTask());
                        }
                    }
                }
            } finally {
                this.rendering.set(false);
            }
        }
    }

    /**
     * 渲染任务
     *
     * @author oyzh
     * @since 2024/3/26
     */
    @Data
    @AllArgsConstructor
    public static class QueueTask {

        /**
         * 任务
         */
        private Runnable task;

        /**
         * 类型
         * 0: 默认线程
         * 1: fx线程
         * 2: fx线程，异步
         */
        private byte type;


    }
}
