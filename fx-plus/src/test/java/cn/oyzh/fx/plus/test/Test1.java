package cn.oyzh.fx.plus.test;

import cn.hutool.core.util.NumberUtil;
import cn.oyzh.fx.common.util.NumUtil;
import org.junit.Test;

import java.math.BigDecimal;
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
        BigDecimal b1 = new BigDecimal(-1L);
        BigDecimal b2 = new BigDecimal(Float.MIN_VALUE);
        System.out.println(NumberUtil.isLess(b1, b2));
        System.out.println(NumberUtil.isLess(b2, b1));
    }

    @Test
    public void test4() {
        BigDecimal b1 = new BigDecimal(-1L);
        BigDecimal b2 = new BigDecimal(Long.MIN_VALUE);
        System.out.println(NumberUtil.isLess(b1, b2));
        System.out.println(NumberUtil.isLess(b2, b1));
    }

    @Test
    public void test5() {
        float d1 = -1.1f;
        float d2 = 1.1f;
        float d3 = Float.MIN_VALUE - 0.000001f;
        // float d4 = -1.2f;
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        BigDecimal b3 = new BigDecimal(d3);
        System.out.println(b1.compareTo(b3));
        System.out.println(b2.compareTo(b3));
        // System.out.println(b2.toPlainString().compareTo(b1.toPlainString()));
        // System.out.println(NumUtil.isLT(d3, d4));
        // System.out.println(NumUtil.isGT(d3, d4));

    }

    @Test
    public void test6() {
        Double d1 = 1d;
        Double d2 = -1d;
        Double d3 = Double.MIN_VALUE;
        // double d3 = Math.pow(2, -1074);
        System.out.println(d2 > d3);
        System.out.println(d2 < d3);
        System.out.println(Double.compare(d1, d3));
        System.out.println(Double.compare(d2, d3));
        System.out.println(NumUtil.isLT(d1, d3));
        System.out.println(NumUtil.isLT(d2, d3));

    }

    @Test
    public void test7() {
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
    public void test8() {
        DecimalFormat df = new DecimalFormat("#.###");

        System.out.println(df.format(0.777999));
    }

}
