// package cn.oyzh.event;
//
// import cn.oyzh.common.log.JulLog;
// import cn.oyzh.common.thread.TaskManager;
// import cn.oyzh.fx.plus.util.FXUtil;
// import com.google.common.eventbus.EventBus;
// import com.google.common.eventbus.SubscriberExceptionHandler;
// import lombok.experimental.UtilityClass;
//
// /**
//  * 事件工具类
//  *
//  * @author oyzh
//  * @since 2023/4/10
//  */
// @UtilityClass
// public class EventUtil {
//
//     /**
//      * 事件总线对象
//      */
//     private static EventBus eventBus;
//
//     /**
//      * 异常处理器
//      */
//     private static SubscriberExceptionHandler exceptionHandler;
//
//     /**
//      * 获取事件总线对象
//      * @return 事件总线对象
//      */
//     private EventBus eventBus() {
//         if (eventBus == null) {
//             synchronized (EventUtil.class) {
//                 eventBus = new EventBus((e, c) -> {
//                     if (exceptionHandler != null) {
//                         exceptionHandler.handleException(e, c);
//                     }
//                 });
//             }
//         }
//         return eventBus;
//     }
//
//     /**
//      * 异常处理器
//      *
//      * @param exceptionHandler 异常处理器
//      */
//     public static void exceptionHandler(SubscriberExceptionHandler exceptionHandler) {
//         EventUtil.exceptionHandler = exceptionHandler;
//     }
//
//     /**
//      * 注册
//      *
//      * @param listener 监听器
//      */
//     public static void register(EventListener listener) {
//         if (listener != null) {
//             eventBus().register(listener);
//         }
//     }
//
//     /**
//      * 取消注册
//      *
//      * @param listener 监听器
//      */
//     public static void unregister(EventListener listener) {
//         if (listener != null) {
//             try {
//                 eventBus().unregister(listener);
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//             }
//         }
//     }
//
//     /**
//      * 发送事件
//      *
//      * @param event 事件
//      */
//     public static void post(Event<?> event) {
//         post(event, EventConfig.DEFAULT);
//     }
//
//     /**
//      * 发送事件
//      *
//      * @param event  事件
//      * @param config 配置
//      */
//     public static void post(Event<?> event, EventConfig config) {
//         if (event == null || config == null) {
//             return;
//         }
//         Runnable func = () -> {
//             Long startTime = null;
//             // 打印日志
//             if (config.isVerbose()) {
//                 startTime = System.currentTimeMillis();
//                 JulLog.debug("post event[type={}] start.", event.getClass());
//             }
//             eventBus().post(event);
//             // 打印日志
//             if (startTime != null) {
//                 long endTime = System.currentTimeMillis();
//                 JulLog.debug("post event:[type={}] finish, cost:{}ms.", event.getClass(), (endTime - startTime));
//             }
//         };
//         // 延迟、异步、fx线程
//         if (config.isDelay() && config.isAsync() && config.isFxThread()) {
//             TaskManager.startDelay(() -> FXUtil.runWait(func), config.getDelay());
//         } else if (config.isDelay() && config.isAsync()) {// 延迟、异步
//             TaskManager.startDelay(func, config.getDelay());
//         } else if (config.isAsync() && config.isFxThread()) {// 异步、fx线程
//             TaskManager.start(() -> FXUtil.runWait(func));
//         } else if (config.isAsync()) {// 异步
//             TaskManager.start(func);
//         } else {// 正常执行
//             func.run();
//         }
//     }
//
//     /**
//      * 触发事件，延迟执行
//      *
//      * @param event 事件
//      * @param delay 延迟事件
//      */
//     public static void postDelay(Event<?> event, int delay) {
//         post(event, EventConfigBuilder.newBuilder().fxThread(true).async(true).delay(delay).build());
//     }
//
//     /**
//      * 触发事件，异步执行
//      *
//      * @param event 事件
//      */
//     public static void postAsync(Event<?> event) {
//         post(event, EventConfigBuilder.newBuilder().async(true).fxThread(true).build());
//     }
// }
//
//
