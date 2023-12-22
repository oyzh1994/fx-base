// package cn.oyzh.fx.plus.task;
//
// import javafx.concurrent.Task;
//
// /**
//  * @author oyzh
//  * @since 2023/8/11
//  */
// public class RunTask extends Task<Object> {
//
//     private final Runnable task;
//
//     public RunTask(Runnable task) {
//         this.task = task;
//     }
//
//     @Override
//     protected Object call() throws Exception {
//         if (this.task != null) {
//             task.run();
//         }
//         return null;
//     }
// }
