// package cn.oyzh.fx.common.thread;
//
// import java.util.TimerTask;
// import java.util.concurrent.Future;
//
// /**
//  * 基础task
//  *
//  * @author oyzh
//  * @since 2023/4/7
//  */
// public abstract class BaseTask<V> extends TimerTask implements Future<V> {
//
//     /**
//      * 执行状态
//      * 0 未运行
//      * 1 运行中
//      * 2 已结束
//      * 3 已取消
//      */
//     protected volatile byte status;
//
//     @Override
//     public boolean cancel() {
//         boolean res = false;
//         try {
//             res = super.cancel();
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         if (res) {
//             this.status = 3;
//         }
//         return res;
//     }
//
//     @Override
//     public boolean cancel(boolean mayInterruptIfRunning) {
//         return this.cancel();
//     }
//
//     @Override
//     public boolean isCancelled() {
//         return this.status == 3;
//     }
//
//     @Override
//     public boolean isDone() {
//         return this.isCancelled() || this.status == 2;
//     }
// }
