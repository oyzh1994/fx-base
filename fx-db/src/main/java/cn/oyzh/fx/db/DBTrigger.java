package cn.oyzh.fx.db;


import cn.oyzh.common.util.StringUtil;

/**
 * @author oyzh
 * @since 2024/1/30
 */
public interface DBTrigger {

    /**
     * 获取名称
     *
     * @return 结果
     */
    String getName();

    /**
     * 设置名称
     *
     * @param name 名称
     */
    void setName(String name);

    /**
     * 是否无效
     *
     * @return 结果
     */
    default boolean isInvalid() {
        return StringUtil.isBlank(this.getName());
    }
}
