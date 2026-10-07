package cn.oyzh.fx.plus.property;


/**
 * 包含三个参数的属性对象
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class Param3Property<P1, P2, P3> {

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
     * 获取参数。
     *
     * @return 参数
     */
    public P3 getParam3() {
        return param3;
    }

    /**
     * 设置参数。
     *
     * @param param3 参数
     */
    public void setParam3(P3 param3) {
        this.param3 = param3;
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
     * 参数3
     */
    private P3 param3;

    /**
     * 构造参数属性对象。
     */
    public Param3Property() {
    }

    /**
     * 构造参数属性对象。
     *
     * @param p1 参数1
     * @param p2 参数2
     * @param p3 p
     */
    public Param3Property(P1 p1, P2 p2, P3 p3) {
        this.param1 = p1;
        this.param2 = p2;
        this.param3 = p3;
    }

    /**
     * 创建属性对象
     *
     * @param p1 参数1
     * @param p2 参数2
     * @param p3 参数3
     * @return 属性对象
     */
    public static <P1, P2, P3> Param3Property<P1, P2, P3> of(P1 p1, P2 p2, P3 p3) {
        return new Param3Property<>(p1, p2, p3);
    }
}
