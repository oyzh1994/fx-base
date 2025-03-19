package cn.oyzh.fx.plus.thread;

import cn.oyzh.fx.plus.util.FXUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 渲染服务，单进程
 *
 * @author oyzh
 * @since 2023/11/28
 */

public class RenderService {

    /**
     * 渲染服务
     */
    private static final ExecutorService RENDER_SERVICE = Executors.newSingleThreadScheduledExecutor();

    /**
     * 提交任务
     *
     * @param task 任务
     */
    public static void submit(Runnable task) {
        if (task != null) {
            RENDER_SERVICE.submit(task);
        }
    }

    /**
     * 提交任务，fx线程
     *
     * @param task 任务
     */
    public static void submitFX(Runnable task) {
        if (task != null) {
            RENDER_SERVICE.submit(() -> FXUtil.runWait(task));
        }
    }

    /**
     * 提交任务，fx线程，并延后处理
     *
     * @param task 任务
     */
    public static void submitFXLater(Runnable task) {
        if (task != null) {
            RENDER_SERVICE.submit(() -> FXUtil.runLater(task));
        }
    }
}
