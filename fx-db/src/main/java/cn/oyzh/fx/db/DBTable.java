package cn.oyzh.fx.db;

import cn.oyzh.common.util.StringUtil;

/**
 *
 * @author oyzh
 * @since 2026-08-21
 */
public interface DBTable extends DBName{

    /**
     * 设置注释
     *
     * @param comment 注释
     */
    void setComment(String comment);

    /**
     * 获取注释
     *
     * @return 结果
     */
    String getComment();

    /**
     * 是否有注释
     *
     * @return 结果
     */
    default boolean hasComment() {
        return this.getComment() != null;
    }

    /**
     * 是否无效
     *
     * @return 结果
     */
    default boolean isInvalid() {
        return StringUtil.isBlank(this.getName());
    }

}
