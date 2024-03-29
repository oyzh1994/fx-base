package cn.oyzh.fx.common.thread;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.WeakCache;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Future;

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
