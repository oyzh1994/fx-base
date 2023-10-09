package cn.oyzh.fx.plus.event;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 事件
 *
 * @author oyzh
 * @since 2023/4/10
 */
@Data
@Accessors(fluent = true, chain = true)
public class Event<D> {

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

}
