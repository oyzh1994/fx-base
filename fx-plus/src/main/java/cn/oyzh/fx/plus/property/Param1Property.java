package cn.oyzh.fx.plus.property;


/**
 * 包含单个参数的属性对象
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class Param1Property<P1> {
    /**
     * 获取参数。
     *
     * @return 参数
     */
    public P1 getParam1() {
        return param1;
    }

    /**
     * 设置参数。
     *
     * @param param1 参数1
     */
    public void setParam1(P1 param1) {
        this.param1 = param1;
    }

    /**
     * 参数1
     */
    private P1 param1;

    /**
     * 构造参数属性对象。
     */
    public Param1Property() {
    }

    /**
     * 构造参数属性对象。
     *
     * @param p1 参数1
     */
    public Param1Property(P1 p1) {
        this.param1 = p1;
    }

    /**
     * 创建属性对象
     *
     * @param p1 参数1
     * @return 属性对象
     */
    public static <P1> Param1Property<P1> of(P1 p1) {
        return new Param1Property<>(p1);
    }
}
