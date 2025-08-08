// package cn.oyzh.fx.plus.swing;
//
// import cn.oyzh.common.thread.ThreadUtil;
// import cn.oyzh.common.util.BooleanUtil;
//
// import javax.swing.SwingUtilities;
// import java.util.ArrayDeque;
// import java.util.Map;
// import java.util.Queue;
// import java.util.concurrent.ConcurrentHashMap;
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
//
//     public final Map<Runnable, Boolean> finished = new ConcurrentHashMap<>();
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
//                 ThreadUtil.sleep(20);
//                 if (SwingUtilities.isEventDispatchThread()) {
//                     func.run();
//                     finished.putIfAbsent(func, true);
//                 } else {
//                     // SwingUtilities.invokeLater(() -> {
//                         func.run();
//                         ThreadUtil.sleep(5);
//                         finished.putIfAbsent(func, true);
//                     // });
//                 }
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//             }
//         }
//     }
//
//     public boolean isFinished(Runnable func) {
//         Boolean b = finished.get(func);
//         return BooleanUtil.isTrue(b);
//     }
// }
