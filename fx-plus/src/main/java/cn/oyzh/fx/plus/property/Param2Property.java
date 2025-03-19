package cn.oyzh.fx.plus.property;


/**
 * @author oyzh
 * @since 2025/01/21
 */
public class Param2Property<P1, P2> {
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

    private P1 param1;

    private P2 param2;

    public Param2Property() {
    }

    public Param2Property(P1 p1, P2 p2) {
        this.param1 = p1;
        this.param2 = p2;
    }

    public static <P1, P2> Param2Property<P1, P2> of(P1 p1, P2 p2) {
        return new Param2Property<>(p1, p2);
    }
}
