package cn.oyzh.fx.plus.test;

import cn.hutool.core.util.NumberUtil;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author oyzh
 * @since 2023/11/22
 */
public class Test1 {

    @Test
    public void test1() {
        System.out.println(new DecimalFormat("#.##").format(20.123456789));
    }

    @Test
    public void test2() {
        System.out.println(NumberUtil.toBigDecimal("11.11xxsdada撒旦撒啊实打实"));
    }

    @Test
    public void test3() {
        double d1 = 1;
        double d2 = -1;
        double d3 = -Double.MAX_VALUE;
//        double d3 = Double.MIN_VALUE;
        double d4 = Double.MIN_NORMAL;
        Long d5 = Long.MIN_VALUE;
        double d6 = Double.MAX_VALUE;
        System.out.println(d2 > d3);
        System.out.println(d2 > d3);
        func(d1, d3);
        func(d2, d3);

        System.out.println(NumberFormat.getInstance().format(d4));

        System.out.printf("%f", d4);
        System.out.println();
        System.out.printf("%s", d5);
        System.out.println();
        System.out.printf("%s", d6);
        System.out.println();
        System.out.printf("%s", -d6);
    }

    protected void func(double d1, double d2) {
        System.out.println(d1 > d2);
    }

    @Test
    public void test4() {
        DecimalFormat df = new DecimalFormat("#.###");

        System.out.println(df.format(0.777999));
    }

}
