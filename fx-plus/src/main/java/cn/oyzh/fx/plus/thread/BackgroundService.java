package cn.oyzh.fx.plus.thread;

import cn.hutool.core.util.RuntimeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 背景服务，多进程
 *
 * @author oyzh
 * @since 2023/12/01
 */
@UtilityClass
public class BackgroundService {

    /**
     * 渲染服务
     */
    private static final ExecutorService BACKGROUND_SERVICE = Executors.newFixedThreadPool(RuntimeUtil.getProcessorCount() * 2);

    /**
     * 提交任务
     *
     * @param task 任务
     */
    public static void submit(Runnable task) {
        if (task != null) {
            BACKGROUND_SERVICE.submit(task);
        }
    }

    /**
     * 提交任务，fx线程
     *
     * @param task 任务
     */
    public static void submitFX(Runnable task) {
        if (task != null) {
            BACKGROUND_SERVICE.submit(() -> FXUtil.runWait(task));
        }
    }

    /**
     * 提交任务，fx线程，并延后处理
     *
     * @param task 任务
     */
    public static void submitFXLater(Runnable task) {
        if (task != null) {
            BACKGROUND_SERVICE.submit(() -> FXUtil.runLater(task));
        }
    }
}
