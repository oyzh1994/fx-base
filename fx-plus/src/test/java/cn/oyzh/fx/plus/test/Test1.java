package cn.oyzh.fx.plus.test;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.event.EventUtil;
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
        System.out.println(cn.oyzh.common.util.NumberUtil.isLT(d1, d3));
        System.out.println(cn.oyzh.common.util.NumberUtil.isLT(d2, d3));

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

    @Test
    public void test9() {
        byte[] val = {21, 101};
        StringBuilder builder = new StringBuilder();
        for (byte b : val) {
            // 将字节与0xFF进行按位与运算，保留最低8位
            int bitValue = b & 0xFF;
            for (int i = 7; i >= 0; i--) {
                builder.append((bitValue & (1 << i)) != 0 ? "1" : "0");
            }
        }
        System.out.println(builder);
    }

    @Test
    public void test10() {
        String bit = "0001010101100101";
        String[] bits = StringUtil.split(bit, 8);

        for (String s : bits) {
            int decimalValue = Integer.parseInt(s, 2); // 将二进制字符串转换为十进制整数
            byte byteValue = Byte.parseByte(String.valueOf(decimalValue)); // 将十进制整数转换为字节
            System.out.println(byteValue);
        }
    }

    @Test
    public void test12() {
        TestEventListener listener = new TestEventListener();
        EventUtil.register(listener);
        TestEvent event = new TestEvent();
        event.data("test----------------->");
        TestEvent2 event2 = new TestEvent2();
        event2.data("test2----------------->");
        TestEvent3 event3 = new TestEvent3();
        event3.data("test3----------------->");
        EventUtil.post(event);
        EventUtil.post(event2);
        EventUtil.post(event3);
    }

}
