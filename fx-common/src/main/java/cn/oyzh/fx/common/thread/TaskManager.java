package cn.oyzh.fx.common.thread;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final Map<String, Future<?>> DELAY_TASKS = new ConcurrentHashMap<>();

    /**
     * 开始延迟任务
     *
     * @param key   唯一标识
     * @param task  任务
     * @param delay 延迟时间
     */
    public static void startDelayTask(@NonNull String key, @NonNull Runnable task, int delay) {
        Future<?> delayTask = DELAY_TASKS.get(key);
        if (delayTask != null && !delayTask.isDone()) {
            ExecutorUtil.cancel(delayTask);
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
        delayTask = ExecutorUtil.start(myTask, delay);
        DELAY_TASKS.put(key, delayTask);
    }
}
