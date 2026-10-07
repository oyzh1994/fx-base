package cn.oyzh.fx.plus.property;


/**
 * 包含两个参数的属性对象
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class Param2Property<P1, P2> {
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
     * 获取参数。
     *
     * @return 参数
     */
    public P2 getParam2() {
        return param2;
    }

    /**
     * 设置参数。
     *
     * @param param2 参数2
     */
    public void setParam2(P2 param2) {
        this.param2 = param2;
    }

    /**
     * 参数1
     */
    private P1 param1;

    /**
     * 参数2
     */
    private P2 param2;

    /**
     * 构造参数属性对象。
     */
    public Param2Property() {
    }

    /**
     * 构造参数属性对象。
     *
     * @param p1 参数1
     * @param p2 参数2
     */
    public Param2Property(P1 p1, P2 p2) {
        this.param1 = p1;
        this.param2 = p2;
    }

    /**
     * 创建属性对象
     *
     * @param p1 参数1
     * @param p2 参数2
     * @return 属性对象
     */
    public static <P1, P2> Param2Property<P1, P2> of(P1 p1, P2 p2) {
        return new Param2Property<>(p1, p2);
    }
}
