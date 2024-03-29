package cn.oyzh.fx.plus.event;

/**
 * 事件消息格式化器
 *
 * @author oyzh
 * @since 2023/9/18
 */
public interface EventFormatter {

    /**
     * 格式化消息
     *
     * @return 格式化后的消息
     */
    String eventFormat();

}
