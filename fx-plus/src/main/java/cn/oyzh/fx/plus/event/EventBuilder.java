package cn.oyzh.fx.plus.event;

/**
 * 事件
 *
 * @author oyzh
 * @since 2023/4/10
 */
public class EventBuilder<D> {

    /**
     * 数据
     */
    private D data;

    /**
     * 类型
     */
    private String type;

    /**
     * 分组名称
     */
    private String group;

    /**
     * 额外数据
     */
    private Object extra;

    public EventBuilder<D> data(D data) {
        this.data = data;
        return this;
    }

    public EventBuilder<D> type(String type) {
        this.type = type;
        return this;
    }

    public EventBuilder<D> group(String group) {
        this.group = group;
        return this;
    }

    public EventBuilder<D> extra(Object extra) {
        this.extra = extra;
        return this;
    }

    public Event<D> build() {
        Event<D> event = new Event<>();
        event.data(data);
        event.type(type);
        event.extra(extra);
        event.group(group);
        return event;
    }

    public static <D> EventBuilder<D> newBuilder() {
        return new EventBuilder<>();
    }

    public static EventBuilder<Object> newBuilder(EventMsg msg) {
        EventBuilder<Object> builder = new EventBuilder<>();
        builder.type(msg.name()).group(msg.group()).data(msg);
        return builder;
    }
}
