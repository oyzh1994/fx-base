package cn.oyzh.fx.plus.property;

import lombok.Data;

/**
 * @author oyzh
 * @since 2025/01/21
 */
@Data
public class Param2Property<P1, P2> {

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
