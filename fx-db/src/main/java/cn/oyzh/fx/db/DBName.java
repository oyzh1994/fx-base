package cn.oyzh.fx.db;

import cn.oyzh.common.util.StringUtil;

/**
 *
 * @author oyzh
 * @since 2026-09-07
 */
public interface DBName {

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
     * 是否新数据
     *
     * @return 结果
     */
    default boolean isNew() {
        return StringUtil.isBlank(this.getName());
    }
}
