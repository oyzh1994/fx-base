package cn.oyzh.fx.db;

import cn.oyzh.common.util.StringUtil;

/**
 * 视图接口，提供视图注释及可更新属性的读写能力
 *
 * @author oyzh
 * @since 2026-09-02
 */
public interface DBView extends DBName{

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
     * 设置可更新
     *
     * @param updatable 可更新
     */
    void setUpdatable(boolean updatable);

    /**
     * 是否可更新
     *
     * @return 结果
     */
    boolean isUpdatable();

    /**
     * 是否无效
     *
     * @return 结果
     */
    default boolean isInvalid() {
        return StringUtil.isBlank(this.getName());
    }

}
