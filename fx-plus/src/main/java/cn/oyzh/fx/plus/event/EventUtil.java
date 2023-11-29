package cn.oyzh.fx.plus.event;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.common.thread.TaskBuilder;
import cn.oyzh.fx.common.thread.TaskManager;
import cn.oyzh.fx.common.thread.ThreadUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * 事件工具类
 *
 * @author oyzh
 * @since 2023/4/10
 */
//@Slf4j
@UtilityClass
public class EventUtil {

    /**
     * 排除方法
     */
    private static final Set<String> EXCLUDES = new HashSet<>();

    /**
     * 事件接收者列表
     */
    private static final Set<WeakReference<Object>> RECEIVERS = new HashSet<>();

    static {
        // 基础
        EXCLUDES.add("wait");
        EXCLUDES.add("hashCode");
        EXCLUDES.add("equals");
        EXCLUDES.add("toString");
        EXCLUDES.add("finalize");
        EXCLUDES.add("getClass");

        // fx
        EXCLUDES.add("computeAreaInScreen");
        EXCLUDES.add("setOnRotationStarted");
        EXCLUDES.add("setOnRotationFinished");
        EXCLUDES.add("setOnScrollFinished");
        EXCLUDES.add("setMouseTransparent");
        EXCLUDES.add("setFocusTraversable");
        EXCLUDES.add("setOnContextMenuRequested");
        EXCLUDES.add("getOnMouseDragEntered");
        EXCLUDES.add("previousSibling");
        EXCLUDES.add("nextSibling");
        EXCLUDES.add("setExpanded");
        EXCLUDES.add("getGraphic");
        EXCLUDES.add("setGraphic");
        EXCLUDES.add("buildEventDispatchChain");
        EXCLUDES.add("addEventFilter");
        EXCLUDES.add("removeEventFilter");
        EXCLUDES.add("isLeaf");
        EXCLUDES.add("getParent");
        EXCLUDES.add("isExpanded");
        EXCLUDES.add("addEventHandler");
        EXCLUDES.add("removeEventHandler");
        EXCLUDES.add("getValue");
        EXCLUDES.add("setValue");
    }

    /**
     * 注册事件处理对象
     *
     * @param obj 事件处理对象
     */
    public static void register(@NonNull Object obj) {
        // 先取消注册对象
        unregister(obj);
        // 重新注册对象
        RECEIVERS.add(new WeakReference<>(obj));
    }

    /**
     * 取消注册事件处理对象
     *
     * @param obj 事件处理对象
     */
    public static void unregister(@NonNull Object obj) {
        RECEIVERS.removeIf(r -> r == null || r.get() == null || r.get() == obj);
    }

    /**
     * 触发事件
     *
     * @param type 类型
     */
    public static void fire(@NonNull String type) {
        fire(EventBuilder.newBuilder().type(type).build());
    }

    /**
     * 触发事件
     *
     * @param type 类型
     */
    public static void fire(@NonNull String type, Object data) {
        fire(EventBuilder.newBuilder().type(type).data(data).build());
    }

    /**
     * 触发事件
     *
     * @param event 事件
     */
    public static void fire(@NonNull Event<?> event) {
        // 遍历对象
        for (WeakReference<Object> reference : RECEIVERS) {
            if (reference == null) {
                continue;
            }
            Object obj = reference.get();
            if (obj == null) {
                continue;
            }
            // 执行方法
            Method[] methods = obj.getClass().getMethods();
            for (Method method : methods) {
                invokeMethod(method, event, obj);
            }
            methods = obj.getClass().getDeclaredMethods();
            for (Method method : methods) {
                invokeMethod(method, event, obj);
            }
        }
    }

    /**
     * 触发事件，延迟执行
     *
     * @param event 事件
     * @param delay 延迟事件
     */
    public static void fireDelay(@NonNull Event<?> event, int delay) {
        // 执行延迟任务
        Task task = TaskBuilder.newBuilder().onStart(() -> fire(event)).build();
        TaskManager.startDelay("event:delay:" + event.type() + ":" + event.group(), task, delay);
    }

    /**
     * 执行方法
     *
     * @param method 方法
     * @param event  事件
     * @param obj    对象
     * @return 结果
     */
    private static boolean invokeMethod(Method method, Event<?> event, Object obj) {
        // 排除一般方法
        if (Modifier.isInterface(method.getModifiers()) || Modifier.isAbstract(method.getModifiers())
                || Modifier.isNative(method.getModifiers()) || Modifier.isStatic(method.getModifiers())
                || method.isDefault() || method.isBridge() || method.getName().contains("$")
                || method.getName().endsWith("Property") || EXCLUDES.contains(method.getName())) {
            return false;
        }
        // 按类型执行
        if (event.type() != null) {
            EventReceiver[] receivers = method.getAnnotationsByType(EventReceiver.class);
            if (handleByType(receivers, event, method, obj)) {
                return true;
            }
        }
        // 按分组执行
        if (event.group() != null) {
            EventGroup[] groups = method.getAnnotationsByType(EventGroup.class);
            return handleByGroup(groups, event, method, obj);
        }
        return false;
    }

    /**
     * 按类型执行
     *
     * @param receivers 接收注解
     * @param event     事件
     * @param method    方法
     * @param obj       对象
     * @return 结果
     */
    private static boolean handleByType(EventReceiver[] receivers, Event<?> event, Method method, Object obj) {
        if (ArrayUtil.isEmpty(receivers)) {
            return false;
        }
        // 是否成功
        boolean success = false;
        // 遍历注解
        for (EventReceiver receiver : receivers) {
            // 初次执行判断
            if (!success) {
                // 判断是否可调用
                if (!method.trySetAccessible()) {
                    StaticLog.warn("trySetAccessible fail obj:{} method:{}.", obj.getClass().getName(), method.getName());
                    return false;
                }
                // 设置可访问
                method.setAccessible(true);
            }
            // 检查类型
            if (receiver.value().equals(event.type())) {
                // 执行方法
                fire(obj, method, event, receiver.verbose(), receiver.fxThread(), receiver.async());
                // 执行成功
                success = true;
            }
        }
        // 返回结果
        return success;
    }

    /**
     * 按分组执行
     *
     * @param groups 分组
     * @param event  事件
     * @param method 方法
     * @param obj    对象
     * @return 结果
     */
    private static boolean handleByGroup(EventGroup[] groups, Event<?> event, Method method, Object obj) {
        if (ArrayUtil.isEmpty(groups)) {
            return false;
        }
        // 是否成功
        boolean success = false;
        // 遍历注解
        for (EventGroup group : groups) {
            // 初次执行判断
            if (!success) {
                // 判断是否可调用
                if (!method.trySetAccessible()) {
                    StaticLog.warn("trySetAccessible fail obj:{} method:{}.", obj.getClass().getName(), method.getName());
                    return false;
                }
                // 设置可访问
                method.setAccessible(true);
            }
            // 检查分组
            if (group.value().equals(event.group())) {
                // 执行方法
                fire(obj, method, event, group.verbose(), group.fxThread(), group.async());
                // 执行成功
                success = true;
            }
        }
        // 返回结果
        return success;
    }

    /**
     * 触发事件
     *
     * @param obj      事件对象
     * @param method   事件方法
     * @param event    事件
     * @param verbose  是否打印详情
     * @param fxThread 是否使用fx线程
     * @param async    是否异步执行
     */
    private static void fire(Object obj, Method method, Event<?> event, boolean verbose, boolean fxThread,
                             boolean async) {
        // 实际调用
        Runnable invoke = () -> {
            Long startTime = null;
            // 打印日志
            if (verbose) {
                startTime = System.currentTimeMillis();
                StaticLog.debug("fire event[type={},group={},async={},fxThread={},method={}] start.", event.type(), event.group(), async, fxThread, method.getName());
            }
            try {
                // 无参
                if (method.getParameterCount() == 0) {
                    method.invoke(obj);
                } else if (method.getParameterCount() == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {// Event参数
                    method.invoke(obj, event);
                } else if (method.getParameterCount() == 1) {// Object参数
                    Class<?> type1 = method.getParameterTypes()[0];
                    if (event.data() != null && type1.isAssignableFrom(event.data().getClass())) {
                        method.invoke(obj, event.data());
                    }
                } else if (method.getParameterCount() == 2 && method.getParameterTypes()[0] == String.class) {// String&Object参数
                    method.invoke(obj, event.type(), event.data());
                } else {
                    StaticLog.error("method:{} invoke error, ParameterTypes invalid!", method.getName());
                }
                // 打印日志
                if (verbose) {
                    long endTime = System.currentTimeMillis();
                    StaticLog.debug("fire event:[class={},method={},type={},group={}] finish, cost:{}ms.", obj.getClass().getName(), method.getName(), event.type(), event.data(), (endTime - startTime));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                StaticLog.warn("fire event:[class={},method={},type={},group={}] fail.", obj.getClass().getName(), method.getName(), event.type(), event.group());
            }
        };
        // 判断调用方式
        if (async && fxThread) {
            ThreadUtil.startVirtual(() -> FXUtil.runLater(invoke));
        } else if (fxThread) {
            FXUtil.runLater(invoke);
        } else if (async) {
            ThreadUtil.startVirtual(invoke);
        } else {
            invoke.run();
        }
    }
}


