package cn.oyzh.fx.common.test;

import cn.oyzh.fx.common.util.NumUtil;
import org.junit.Test;

/**
 * @author oyzh
 * @since 2024/8/14
 */
public class NumUtilTest {

    @Test
    public void test1() {
        System.out.println(NumUtil.checkBound(5, 10, 5, 10));
        System.out.println(NumUtil.checkBound(5, 10, 6, 9));
        System.out.println(NumUtil.checkBound(5, 10, 4, 9));
        System.out.println(NumUtil.checkBound(5, 10, 4, 10));
        System.out.println(NumUtil.checkBound(5, 10, 5, 11));
        System.out.println(NumUtil.checkBound(5, 10, 0, 4));
    }

    @Test
    public void test2() {
        System.out.println(NumUtil.checkBound(168, 184, 159, 194));
    }

    @Test
    public void test3() {
        System.out.println(NumUtil.checkBound(5, 10, 0, 4));
    }

    @Test
    public void test4() {
        System.out.println(NumUtil.checkBound(5, 10, 11, 15));
    }
}
