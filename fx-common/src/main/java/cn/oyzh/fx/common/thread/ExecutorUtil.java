package cn.oyzh.fx.common.thread;

import lombok.experimental.UtilityClass;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 业务执行工具类
 *
 * @author oyzh
 * @since 2023/9/27
 */
@UtilityClass
public class ExecutorUtil {

    /**
     * 定时业务执行器
     */
    private static ScheduledExecutorService executor;

    /**
     * 获取业务执行器
     *
     * @return 业务执行器
     */
    public static ScheduledExecutorService executor() {
        if (executor == null) {
            executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        }
        return executor;
    }

    /**
     * 提交任务
     *
     * @param task 任务
     * @return 任务
     */
    public static Future<?> submit(Runnable task) {
       return executor().submit(task);
    }

    /**
     * 开始任务
     *
     * @param task  任务
     * @param delay 延迟
     * @return 任务执行结果
     */
    public static ScheduledFuture<?> start(Runnable task, long delay) {
        return executor().schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 开始任务
     *
     * @param task   任务
     * @param delay  延迟
     * @param period 定时周期
     * @return 任务执行结果
     */
    public static ScheduledFuture<?> start(Runnable task, long delay, long period) {
        return executor().scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行任务
     *
     * @param task  任务
     * @param delay 延迟
     * @return 任务执行结果
     */
    public static <V> ScheduledFuture<V> invoke(Callable<V> task, long delay) {
        return executor().schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 取消任务
     *
     * @param task 任务
     */
    public static void cancel(Future<?> task) {
        try {
            if (task != null && !task.isDone() && !task.isCancelled()) {
                task.cancel(true);
            }
        } catch (Exception ignored) {

        }
    }
}
