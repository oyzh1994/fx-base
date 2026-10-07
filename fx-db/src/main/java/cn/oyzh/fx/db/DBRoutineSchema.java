package cn.oyzh.fx.db;


import cn.oyzh.common.util.StringUtil;

/**
 * 存储过程/函数模式接口，用于校验名称是否有效
 *
 * @author oyzh
 * @since 2024/1/30
 */
public interface DBRoutineSchema extends DBName{

    /**
     * 是否无效
     *
     * @return 结果
     */
    default boolean isInvalid() {
        return StringUtil.isBlank(this.getName());
    }

}
