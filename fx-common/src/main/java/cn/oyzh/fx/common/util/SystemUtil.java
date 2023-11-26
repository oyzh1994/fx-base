package cn.oyzh.fx.common.util;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.common.thread.ThreadUtil;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Future;

/**
 * 系统工具类
 *
 * @author oyzh
 * @since 2023/04/05
 */
//@Slf4j
@UtilityClass
public class SystemUtil {

    /**
     * gc定期任务
     */
    private static Future<?> GC_INTERVAL_TASK;

    /**
     * 执行gc
     */
    public static void gc() {
        try {
            long maxMemory = Runtime.getRuntime().maxMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            long usedMemory = totalMemory - freeMemory;
            StaticLog.info(
                    "gc before freeMemory:{}Mb usedMemory:{}Mb totalMemory:{}Mb maxMemory:{}Mb",
                    freeMemory / 1024 / 1024.0,
                    usedMemory / 1024 / 1024.0,
                    totalMemory / 1024 / 1024.0,
                    maxMemory / 1024 / 1024.0
            );
            System.gc();
            freeMemory = Runtime.getRuntime().freeMemory();
            totalMemory = Runtime.getRuntime().totalMemory();
            usedMemory = totalMemory - freeMemory;
            StaticLog.info(
                    "gc after freeMemory:{}Mb usedMemory:{}Mb totalMemory:{}Mb",
                    freeMemory / 1024 / 1024.0,
                    usedMemory / 1024 / 1024.0,
                    totalMemory / 1024 / 1024.0
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 定期gc
     */
    public static void gcInterval(int interval) {
        clearGCInterval();
//        if (log.isDebugEnabled()) {
            StaticLog.debug("gc interval in {}ms...", interval);
//        }
        GC_INTERVAL_TASK = ExecutorUtil.start(SystemUtil::gc, interval, interval);
    }

    /**
     * 清除定期gc任务
     */
    public static void clearGCInterval() {
        if (GC_INTERVAL_TASK != null && !GC_INTERVAL_TASK.isDone()) {
            ExecutorUtil.cancel(GC_INTERVAL_TASK);
//            if (log.isDebugEnabled()) {
                StaticLog.debug("cancel gc interval task.");
//            }
        }
    }

    /**
     * 延迟gc
     */
    public static void gcLater() {
        ThreadUtil.startVirtual(SystemUtil::gc);
    }
}
