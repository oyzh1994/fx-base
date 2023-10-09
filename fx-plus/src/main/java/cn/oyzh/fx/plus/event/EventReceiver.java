package cn.oyzh.fx.plus.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件接收者注解
 *
 * @author oyzh
 * @since 2023/4/10
 */
@Repeatable(EventReceivers.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventReceiver {

    /**
     * 事件类型
     *
     * @return 事件类型
     */
    String value();

    /**
     * 异步执行
     *
     * @return 结果
     */
    boolean async() default false;

    /**
     * 详细信息
     *
     * @return 结果
     */
    boolean verbose() default false;

    /**
     * 使用fx线程
     *
     * @return 结果
     */
    boolean fxThread() default false;

    /**
     * 执行顺序，数字越大，执行顺序越前
     *
     * @return 顺序值
     */
    int order() default Integer.MIN_VALUE;
}
