package cn.oyzh.fx.common.util;

/**
 * 对象复制器
 *
 * @author oyzh
 * @since 2024/07/03
 */
public interface ObjectCopier<T> {

    /**
     * 复制对象
     *
     * @param t1 待复制对象
     */
    void copy(T t1);
}
