package cn.oyzh.fx.plus.test;

import cn.hutool.core.util.NumberUtil;
import cn.oyzh.fx.plus.controls.textfield.DecimalTextField;
import org.junit.Test;

import java.text.DecimalFormat;

/**
 * @author oyzh
 * @since 2023/11/22
 */
public class Test1 {

    @Test
    public void test1(){
        System.out.println(new DecimalFormat("#.##").format(20.123456789));
    }

    @Test
    public void test2(){
        System.out.println(NumberUtil.toBigDecimal("11.11xxsdada撒旦撒啊实打实"));
    }
}
