package cn.oyzh.fx.db;


import cn.oyzh.common.util.StringUtil;

/**
 * 触发器接口，用于校验触发器名称是否有效
 *
 * @author oyzh
 * @since 2026-09-02
 */
public interface DBTrigger extends DBName {

    /**
     * 是否无效
     *
     * @return 结果
     */
    default boolean isInvalid() {
        return StringUtil.isBlank(this.getName());
    }
}
