package cn.oyzh.fx.common.util;// package cn.oyzh.common.util;
//
// import lombok.NonNull;
// import lombok.experimental.UtilityClass;
//
// import java.util.Timer;
//
// /**
//  * 定时器工具类
//  *
//  * @author oyzh
//  * @since 2022/12/30
//  */
// @UtilityClass
// public class Timers {
//
//     /**
//      * 执行任务
//      *
//      * @param runnable 业务
//      * @return TimerTask
//      */
//     public static WaitTask newTask(@NonNull Runnable runnable) {
//         return newTask(runnable, 0);
//     }
//
//     /**
//      * 执行任务
//      *
//      * @param runnable 业务
//      * @param delay    延迟时间
//      * @return TimerTask
//      */
//     public static WaitTask newTask(@NonNull Runnable runnable, int delay) {
//         WaitTask task = new WaitTask(runnable);
//         new Timer().schedule(task, delay);
//         return task;
//     }
//
//     /**
//      * 执行任务
//      *
//      * @param runnable 业务
//      * @param delay    延迟时间
//      * @param period   重复间隔
//      * @return TimerTask
//      */
//     public static WaitTask newTask(@NonNull Runnable runnable, int delay, int period) {
//         WaitTask task = new WaitTask(runnable);
//         new Timer().schedule(task, delay, period);
//         return task;
//     }
//
//     /**
//      * 执行定时任务
//      *
//      * @param runnable 业务
//      * @param period   重复间隔
//      * @return TimerTask
//      */
//     public static WaitTask newPeriodTask(@NonNull Runnable runnable, int period) {
//         return newTask(runnable, 0, period);
//     }
// }
