package cn.oyzh.fx.db;


import cn.oyzh.common.util.StringUtil;

/**
 * 数据库接口，用于校验数据库名称是否有效
 *
 * @author oyzh
 * @since 2024/1/30
 */
public interface DBDatabse extends DBName{

    /**
     * 是否无效
     *
     * @return 结果
     */
    default boolean isInvalid() {
        return StringUtil.isBlank(this.getName());
    }
}
