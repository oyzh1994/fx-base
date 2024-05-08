package cn.oyzh.fx.common.util;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.common.thread.TaskManager;
import cn.oyzh.fx.common.thread.ThreadUtil;
import lombok.experimental.UtilityClass;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * 系统工具类
 *
 * @author oyzh
 * @since 2023/04/05
 */
@UtilityClass
public class SystemUtil {

    /**
     * 执行gc
     */
    public static void gc() {
        try {
            // 获取 MemoryMXBean 实例
            MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
            // 获取堆内存信息
            MemoryUsage heapMemoryUsage = mxBean.getHeapMemoryUsage();
            // 获取非堆内存信息
            MemoryUsage nonHeapMemoryUsage = mxBean.getNonHeapMemoryUsage();
            long usedMemory = heapMemoryUsage.getUsed() + nonHeapMemoryUsage.getUsed();
            StaticLog.info("gc之前预估使用内存:{}Mb", usedMemory / 1024 / 1024.0 + 256);
            System.gc();
            usedMemory = heapMemoryUsage.getUsed() + nonHeapMemoryUsage.getUsed();
            StaticLog.info("gc之前预估使用内存:{}Mb", usedMemory / 1024 / 1024.0 + 256);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 定期gc
     */
    public static void gcInterval(int interval) {
        StaticLog.debug("gc interval in {}ms", interval);
        TaskManager.cancelInterval("gc:task");
        TaskManager.startInterval("gc:task", SystemUtil::gc, interval);
    }

    /**
     * 延迟gc
     */
    public static void gcLater() {
        ThreadUtil.startVirtual(SystemUtil::gc);
    }
}
