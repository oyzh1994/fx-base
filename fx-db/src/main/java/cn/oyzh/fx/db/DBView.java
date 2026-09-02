package cn.oyzh.fx.db;

/**
 *
 * @author oyzh
 * @since 2026-08-21
 */
public interface DBView {

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

}
