package cn.oyzh.fx.common.thread;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.WeakCache;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 任务管理器
 *
 * @author oyzh
 * @since 2023/9/27
 */
@UtilityClass
public class TaskManager {

    /**
     * 延迟任务列表
     */
    private static final WeakCache<String, Future<?>> DELAY_TASKS = CacheUtil.newWeakCache(-1);

    /**
     * 循环任务列表
     */
    private static final WeakCache<String, Future<?>> INTERVAL_TASKS = CacheUtil.newWeakCache(-1);

    /**
     * 开始延迟任务
     *
     * @param key   唯一标识
     * @param task  任务
     * @param delay 延迟时间
     */
    public static void startDelay(@NonNull String key, @NonNull Runnable task, int delay) {
        Future<?> future = DELAY_TASKS.get(key);
        if (future != null && !future.isDone()) {
            ExecutorUtil.cancel(future);
        }
        Task myTask;
        if (task instanceof Task task1) {
            myTask = TaskBuilder.newBuilder()
                    .from(task1)
                    .onFinish(() -> {
                        DELAY_TASKS.remove(key);
                        if (task1.getFinish() != null) {
                            task1.getFinish().run();
                        }
                    })
                    .build();
        } else {
            myTask = TaskBuilder.newBuilder()
                    .onStart(task)
                    .onFinish(() -> DELAY_TASKS.remove(key))
                    .build();
        }
        future = ExecutorUtil.start(myTask, delay);
        DELAY_TASKS.put(key, future);
    }

    /**
     * 取消延迟任务
     *
     * @param key 唯一标识
     */
    public static void cancelDelay(@NonNull String key) {
        Future<?> future = DELAY_TASKS.get(key);
        if (future != null) {
            if (!future.isDone()) {
                ExecutorUtil.cancel(future);
            }
            DELAY_TASKS.remove(key);
        }
    }

    /**
     * 开始定时任务
     *
     * @param key      唯一标识
     * @param task     任务
     * @param interval 定时时间
     */
    public static void startInterval(@NonNull String key, @NonNull Runnable task, int interval) {
        Future<?> future = INTERVAL_TASKS.get(key);
        if (future != null && !future.isDone()) {
            ExecutorUtil.cancel(future);
        }
        future = ExecutorUtil.start(task, 0, interval);
        INTERVAL_TASKS.put(key, future);
    }

    /**
     * 取消定时任务
     *
     * @param key 唯一标识
     */
    public static void cancelInterval(@NonNull String key) {
        Future<?> future = INTERVAL_TASKS.get(key);
        if (future != null) {
            if (!future.isDone()) {
                ExecutorUtil.cancel(future);
            }
            INTERVAL_TASKS.remove(key);
        }
    }

    /**
     * 开始延迟任务
     *
     * @param task  任务
     * @param delay 延迟时间
     * @return 任务
     */
    public static Future<?> startDelay(Runnable task, int delay) {
        if (task != null) {
            return ExecutorUtil.start(task, delay);
        }
        return null;
    }

    /**
     * 开始超时任务
     *
     * @param task    任务
     * @param timeout 超时时间
     */
    public static void startTimeout(Runnable task, int timeout) {
        if (task != null) {
            // 创建一个异步任务
            CompletableFuture<Void> future = CompletableFuture.runAsync(task, ExecutorUtil.executor());
            // 等待异步任务完成，如果超时则抛出异常
            try {
                future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                // 如果任务超时，这里会被执行
                future.cancel(true);
            }
        }
    }

    /**
     * 开始任务
     *
     * @param task 任务
     * @return 任务
     */
    public static Future<?> start(Runnable task) {
        if (task != null) {
            return ExecutorUtil.submit(task);
        }
        return null;
    }

    /**
     * 取消任务
     *
     * @param future 任务
     */
    public static void cancel(Future<?> future) {
        try {
            if (future != null && !future.isDone()) {
                future.exceptionNow();
                future.cancel(true);
            }
        } catch (Throwable ignore) {

        }
    }
}
