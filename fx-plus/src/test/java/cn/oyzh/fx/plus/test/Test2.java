package cn.oyzh.fx.plus.test;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-20
 */
public class Test2 {

    public Map<String, Double> getMap1() {
        Map map = new HashMap();
        map.put("a", new BigDecimal("1.0"));
        map.put("b", new BigDecimal("2.0"));
        return map;
    }

    public Map<String, Double> getMap2() {
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("a", new BigDecimal("1.0"));
        map.put("b", new BigDecimal("2.0"));
        return (Map) map;
    }

    public Map<String, Double> getMap3() {
        Map<String, Number> map = new HashMap<>();
        map.put("a", new BigDecimal("1.0"));
        map.put("b", new BigDecimal("2.0"));
        return (Map) map;
    }

    @Test
    public void test3() {
        String str = "1a2a3a";
        String[] arr = str.split("a");
        System.out.println(Arrays.toString(arr));
    }

    @Test
    public void test4() {
        String str = "/112";
        String reg = "112";
        String[] arr = str.split(reg, -1);
        System.out.println(Arrays.toString(arr));
        StringBuilder builder = new StringBuilder();
        for (String s : arr) {
            if (s.isEmpty()) {
                builder.append(reg);
            } else {
                builder.append(s);
            }
        }
        System.out.println(builder.toString());

    }

    @Test
    public void test5() {
//        String str = "/11111234111ads";
//        String reg = "(?i)A";
        String str = "/yjcloud/asr/timer/suspendMonitor ";
        String reg = "(?i)dm";
        String[] arr = str.splitWithDelimiters(reg, -1);
        System.out.println(Arrays.toString(arr));
        StringBuilder builder = new StringBuilder();
        for (String s : arr) {
            builder.append(s);
        }
        System.out.println(builder);

    }
}
