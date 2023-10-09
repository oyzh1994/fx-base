package cn.oyzh.fx.common.util;// package cn.oyzh.common.util;
//
// import cn.hutool.core.thread.ThreadUtil;
// import lombok.NonNull;
//
// import java.util.TimerTask;
//
// /**
//  * 等待完成的task
//  *
//  * @author oyzh
//  * @since 2023/1/5
//  */
// public class WaitTask extends TimerTask {
//
//     /**
//      * 执行状态
//      * 0 未运行
//      * 1 运行中
//      * 2 已结束
//      */
//     private volatile int status;
//
//     /**
//      * 待执行的业务
//      */
//     private final Runnable task;
//
//     /**
//      * 状态锁
//      */
//     private final Object statusLock = new Object();
//
//     public WaitTask(@NonNull Runnable callback) {
//         this.task = callback;
//     }
//
//     @Override
//     public void run() {
//         synchronized (this.statusLock) {
//             this.status = 1;
//         }
//         try {
//             this.task.run();
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         synchronized (this.statusLock) {
//             this.status = 2;
//         }
//     }
//
//     /**
//      * 等待执行完成
//      */
//     public void waitFinish() {
//         while (this.status != 2) {
//             ThreadUtil.safeSleep(5);
//         }
//     }
//
//     /**
//      * 是否未开始
//      *
//      * @return 结果
//      */
//     public boolean isUninitialized() {
//         return this.status == 0;
//     }
//
//     /**
//      * 是否运行中
//      *
//      * @return 结果
//      */
//     public boolean isRunning() {
//         return this.status == 1;
//     }
//
//     /**
//      * 是否已结束
//      *
//      * @return 结果
//      */
//     public boolean isFinish() {
//         return this.status == 2;
//     }
//
//     @Override
//     public boolean cancel() {
//         try {
//             return super.cancel();
//         } catch (Exception ignore) {
//         }
//         return false;
//     }
// }
