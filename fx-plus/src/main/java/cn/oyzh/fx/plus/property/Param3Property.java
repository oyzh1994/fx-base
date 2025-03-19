package cn.oyzh.fx.plus.property;


/**
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class Param3Property<P1, P2, P3> {

    public P1 getParam1() {
        return param1;
    }

    public void setParam1(P1 param1) {
        this.param1 = param1;
    }

    public P2 getParam2() {
        return param2;
    }

    public void setParam2(P2 param2) {
        this.param2 = param2;
    }

    public P3 getParam3() {
        return param3;
    }

    public void setParam3(P3 param3) {
        this.param3 = param3;
    }

    private P1 param1;

    private P2 param2;

    private P3 param3;

    public Param3Property() {
    }

    public Param3Property(P1 p1, P2 p2, P3 p3) {
        this.param1 = p1;
        this.param2 = p2;
        this.param3 = p3;
    }

    public static <P1, P2, P3> Param3Property<P1, P2, P3> of(P1 p1, P2 p2, P3 p3) {
        return new Param3Property<>(p1, p2, p3);
    }
}
