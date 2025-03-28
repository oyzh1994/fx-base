package cn.oyzh.fx.plus.property;


/**
 * @author oyzh
 * @since 2025/01/21
 */
public class Param1Property<P1> {
    public P1 getParam1() {
        return param1;
    }

    public void setParam1(P1 param1) {
        this.param1 = param1;
    }

    private P1 param1;

    public Param1Property() {
    }

    public Param1Property(P1 p1) {
        this.param1 = p1;
    }

    public static <P1> Param1Property<P1> of(P1 p1) {
        return new Param1Property<>(p1);
    }
}
