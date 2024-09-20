package cn.oyzh.fx.plus.test;

import java.math.BigDecimal;
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
}
