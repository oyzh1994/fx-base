package cn.oyzh.fx.common.util;

/**
 * 对象比较器
 *
 * @author oyzh
 * @since 2023/4/24
 */
public interface ObjectComparator<T> {

    /**
     * 比较对象
     *
     * @param t1 待比较对象
     * @return 结果
     */
    boolean compare(T t1);
}
