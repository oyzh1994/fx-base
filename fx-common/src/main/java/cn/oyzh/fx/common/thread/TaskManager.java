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
    private static final WeakCache<String, Future<?>> CACHE = CacheUtil.newWeakCache(-1);

    /**
     * 开始延迟任务
     *
     * @param key   唯一标识
     * @param task  任务
     * @param delay 延迟时间
     */
    public static void startDelay(@NonNull String key, @NonNull Runnable task, int delay) {
        Future<?> delayTask = CACHE.get(key);
        if (delayTask != null && !delayTask.isDone()) {
            ExecutorUtil.cancel(delayTask);
        }
        Task myTask;
        if (task instanceof Task task1) {
            myTask = TaskBuilder.newBuilder()
                    .from(task1)
                    .onFinish(() -> {
                        CACHE.remove(key);
                        if (task1.getFinish() != null) {
                            task1.getFinish().run();
                        }
                    })
                    .build();
        } else {
            myTask = TaskBuilder.newBuilder()
                    .onStart(task)
                    .onFinish(() -> CACHE.remove(key))
                    .build();
        }
        delayTask = ExecutorUtil.start(myTask, delay);
        CACHE.put(key, delayTask);
    }

    /**
     * 开始延迟任务
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    public static void startDelay(Runnable task, int delay) {
        if (task != null) {
            ExecutorUtil.start(task, delay);
        }
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
     */
    public static void start(Runnable task) {
        if (task != null) {
            ExecutorUtil.submit(task);
        }
    }
}
