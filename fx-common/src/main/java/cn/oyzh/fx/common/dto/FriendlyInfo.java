package cn.oyzh.fx.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 友好信息对象
 *
 * @author oyzh
 * @since 2020/3/26
 */
@Data
@Accessors(fluent = true, chain = true)
public class FriendlyInfo<T> {

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private Object value;

    /**
     * 原始值
     */
    private T originalValue;

    /**
     * 友好名称
     */
    private String friendlyName;

    /**
     * 友好值
     */
    private Object friendlyValue;

    /**
     * 获取名称
     *
     * @param friendly 友好化的
     * @return 名称
     */
    public String getName(boolean friendly) {
        return friendly ? this.friendlyName : this.name;
    }

    /**
     * 获取值
     *
     * @param friendly 友好化的
     * @return 值
     */
    public Object getValue(boolean friendly) {
        return friendly ? this.friendlyValue : this.value;
    }
}
