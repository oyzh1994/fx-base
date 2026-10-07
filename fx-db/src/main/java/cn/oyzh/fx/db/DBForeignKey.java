package cn.oyzh.fx.db;


import cn.oyzh.common.util.StringUtil;

/**
 * 外键接口，用于校验外键名称是否有效
 *
 * @author oyzh
 * @since 2026-09-02
 */
public interface DBForeignKey extends DBName {

    /**
     * 是否无效
     *
     * @return 结果
     */
    default boolean isInvalid() {
        return StringUtil.isBlank(this.getName());
    }
}
