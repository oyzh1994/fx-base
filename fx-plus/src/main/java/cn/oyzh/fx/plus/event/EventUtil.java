package cn.oyzh.fx.plus.event;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.oyzh.fx.common.thread.ThreadUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 事件工具类
 *
 * @author oyzh
 * @since 2023/4/10
 */
@Slf4j
@UtilityClass
public class EventUtil {

    /**
     * 事件接收者列表
     */
    private static final List<WeakReference<Object>> RECEIVERS = new ArrayList<>();

    /**
     * 注册事件处理对象
     *
     * @param obj 事件处理对象
     */
    public static void register(@NonNull Object obj) {
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
            // 获取方法
            Method[] methods = ReflectUtil.getMethods(obj.getClass());
            if (ArrayUtil.isEmpty(methods)) {
                continue;
            }
            List<Method> methodList = Stream.of(methods).filter(EventUtil::filterMethod).toList();
            // 遍历并处理方法
            for (Method method : methodList) {
                // 类型处理
                if (event.type() != null) {
                    EventReceiver[] receivers = method.getAnnotationsByType(EventReceiver.class);
                    if (ArrayUtil.isNotEmpty(receivers)) {
                        handleByType(receivers, event, method, obj);
                    }
                }
                // 分组处理
                if (event.group() != null) {
                    EventGroup[] groups = method.getAnnotationsByType(EventGroup.class);
                    if (ArrayUtil.isNotEmpty(groups)) {
                        handleByGroup(groups, event, method, obj);
                    }
                }
            }
        }
    }

    private static boolean filterMethod(Method method) {
        // 排除一般方法
        if (Modifier.isInterface(method.getModifiers()) || Modifier.isAbstract(method.getModifiers())
                || Modifier.isNative(method.getModifiers()) || Modifier.isStatic(method.getModifiers())
                || method.isDefault() || method.isBridge()) {
            return false;
        }
        EventReceiver[] receivers = method.getAnnotationsByType(EventReceiver.class);
        if (receivers.length > 0) {
            return true;
        }
        EventGroup[] groups = method.getAnnotationsByType(EventGroup.class);
        if (groups.length > 0) {
            return true;
        }
        return false;
    }

    private static void handleByType(EventReceiver[] receivers, Event<?> event, Method method, Object obj) {
        List<EventReceiver> list = new ArrayList<>();
        for (EventReceiver receiver : receivers) {
            // 检查类型
            if (receiver.value().equals(event.type())) {
                list.add(receiver);
            }
        }
        if (list.isEmpty()) {
            return;
        }
        // 判断是否可调用
        if (!method.trySetAccessible()) {
            log.warn("trySetAccessible fail obj:{} method:{}.", obj.getClass().getName(), method.getName());
            return;
        }
        // 设置可访问
        method.setAccessible(true);
        // 处理并添加到集合
        for (EventReceiver receiver : receivers) {
            fire(obj, method, event, receiver.verbose(), receiver.fxThread(), receiver.async());
        }
    }

    private static void handleByGroup(EventGroup[] groups, Event<?> event, Method method, Object obj) {
        List<EventGroup> list = new ArrayList<>();
        for (EventGroup group : groups) {
            // 检查类型
            if (group.value().equals(event.group())) {
                list.add(group);
            }
        }
        if (list.isEmpty()) {
            return;
        }
        // 判断是否可调用
        if (!method.trySetAccessible()) {
            log.warn("trySetAccessible fail obj:{} method:{}.", obj.getClass().getName(), method.getName());
            return;
        }
        // 设置可访问
        method.setAccessible(true);
        // 处理并添加到集合
        for (EventGroup group : list) {
            fire(obj, method, event, group.verbose(), group.fxThread(), group.async());
        }
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
    private static void fire(Object obj, Method method, Event<?> event, boolean verbose, boolean fxThread, boolean async) {
        // 实际调用
        Runnable invoke = () -> {
            Long startTime = null;
            // 打印日志
            if (verbose && log.isDebugEnabled()) {
                startTime = System.currentTimeMillis();
                log.debug("fire event[type={},group={},async={},fxThread={},method={}] start.", event.type(), event.group(), async, fxThread, method.getName());
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
                    log.error("method:{} invoke error, ParameterTypes invalid!", method.getName());
                }
                // 打印日志
                if (verbose && startTime != null & log.isDebugEnabled()) {
                    long endTime = System.currentTimeMillis();
                    log.debug("fire event:[class={},method={},type={},group={}] finish, cost:{}ms.", obj.getClass().getName(), method.getName(), event.type(), event.data(), (endTime - startTime));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                log.warn("fire event:[class={},method={},type={},group={}] fail.", obj.getClass().getName(), method.getName(), event.type(), event.group());
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


