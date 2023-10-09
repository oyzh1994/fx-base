package cn.oyzh.fx.common.thread;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 仅执行的任务
 *
 * @author oyzh
 * @since 2023/1/5
 */
public class RunnableTask extends BaseTask<Object> {

    /**
     * 任务名称
     */
    @Setter
    @Getter
    private String name;

    /**
     * 结束处理
     */
    @Getter
    private Runnable onFinish;

    /**
     * 待执行的业务
     */
    private final Runnable task;

    /**
     * 状态锁
     */
    private final Object statusLock = new Object();

    public RunnableTask(@NonNull Runnable task) {
        this.task = task;
    }

    public RunnableTask(@NonNull Runnable task, @NonNull Runnable onFinish) {
        this.task = task;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        synchronized (this.statusLock) {
            this.status = 1;
        }
        try {
            this.task.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        synchronized (this.statusLock) {
            this.status = 2;
        }
        if (this.onFinish != null) {
            try {
                this.onFinish.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long timeout,@NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
