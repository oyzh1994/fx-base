package cn.oyzh.fx.pkg.test;

import org.junit.Test;

import java.util.Comparator;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/6/17
 */
public class Test1 {

    @Test
    public void test() {
        List<Integer> list = new java.util.ArrayList<>(List.of(1, 22, 33, 11, 5654, 3424, 4234, 21342343, 25423, 53, 42));
        // list.sort(Integer::compareTo);
        list.sort(Comparator.reverseOrder());
        System.out.println(list);
    }
}
