package cn.oyzh.fx.common;

/**
 * 解析器
 *
 * @author oyzh
 * @since 2022/6/2
 */
@FunctionalInterface
public interface Parser<T, R> {

    /**
     * 解析
     *
     * @param t 参数
     * @return 结果
     */
    R parse(T t);

}
