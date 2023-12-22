// package cn.oyzh.fx.plus.task;
//
// import javafx.concurrent.Task;
//
// import java.util.concurrent.Callable;
//
// /**
//  * @author oyzh
//  * @since 2023/8/11
//  */
// public class CallTask<V> extends Task<V> {
//
//     private final Callable<V> task;
//
//     public CallTask(Callable<V> task) {
//         this.task = task;
//     }
//
//     @Override
//     protected V call() throws Exception {
//         if (this.task != null) {
//             return this.task.call();
//         }
//         return null;
//     }
// }
