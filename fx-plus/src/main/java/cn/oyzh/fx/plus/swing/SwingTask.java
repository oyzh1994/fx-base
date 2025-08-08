// package cn.oyzh.fx.plus.swing;
//
// import cn.oyzh.common.thread.ThreadUtil;
//
// import javax.swing.SwingUtilities;
// import java.util.ArrayDeque;
// import java.util.Queue;
//
// /**
//  * 专门用于执行swing的ui任务
//  *
//  * @author oyzh
//  * @since 2025-08-07
//  */
// public class SwingTask extends Thread {
//
//     /**
//      * 当前实例
//      */
//     private static SwingTask INSTANCE;
//
//     /**
//      * 获取实例
//      *
//      * @return 实例
//      */
//     public static SwingTask getInstance() {
//         if (INSTANCE == null) {
//             synchronized (SwingTask.class) {
//                 INSTANCE = new SwingTask();
//                 ThreadUtil.startVirtual(INSTANCE);
//             }
//         }
//         return INSTANCE;
//     }
//
//     /**
//      * 任务队列
//      */
//     private final Queue<Runnable> queue = new ArrayDeque<>();
//
//     /**
//      * 添加任务
//      *
//      * @param func 任务
//      */
//     public void addTask(Runnable func) {
//         if (func != null) {
//             this.queue.add(func);
//         }
//     }
//
//     @Override
//     public void run() {
//         while (true) {
//             if (this.queue.isEmpty()) {
//                 ThreadUtil.sleep(5);
//                 continue;
//             }
//             Runnable func = this.queue.poll();
//             if (func == null) {
//                 ThreadUtil.sleep(5);
//                 continue;
//             }
//             try {
//                 if (SwingUtilities.isEventDispatchThread()) {
//                     func.run();
//                 } else {
//                     SwingUtilities.invokeAndWait(func);
//                 }
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//             }
//         }
//     }
// }
