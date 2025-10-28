package cn.oyzh.fx.pkg.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.common.util.StringUtil;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
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

    @Test
    public void test2() {
        int i1 = StrUtil.compare("1", "123", true);
        int i2 = StringUtil.compare("1", "123", true);
        System.out.println(i1 == i2);
    }

    @Test
    public void test3() {
        String s1 = StrUtil.replaceLast("1223", "22", "4");
        String s2 = StringUtil.replaceLast("1223", "22", "4");
        System.out.println(s1);
        System.out.println(s2);
    }

    @Test
    public void test4() {
        byte[] bytes = "Abca1".getBytes();
        String s1 = HexUtil.encodeHexStr(bytes, false);
        String s11 = HexUtil.encodeHexStr(bytes, true);
        String s2 = cn.oyzh.common.util.HexUtil.encodeHexStr(bytes, false);
        String s22 = cn.oyzh.common.util.HexUtil.encodeHexStr(bytes, true);
        System.out.println(s1);
        System.out.println(s11);
        System.out.println(s2);
        System.out.println(s22);
    }

    @Test
    public void test5() {
        String s1 = "abc";
        String s2 = "ab1c";
        double d1 = StrUtil.similar(s1, s2);
        double d2 = StringUtil.similarity(s1, s2);
        System.out.println(d1);
        System.out.println(d2);
    }

    @Test
    public void test6() {
        String d1 = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String d11 = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
        String d2 = cn.oyzh.common.date.DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String d21 = cn.oyzh.common.date.DateUtil.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
        String d22 = cn.oyzh.common.date.DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
        System.out.println(d1);
        System.out.println(d11);
        System.out.println(d2);
        System.out.println(d21);
        System.out.println(d22);
    }

    @Test
    public void test7() {
        String date = "2022-11-11 15:15:15";
        LocalDateTime d1 = DateUtil.parseLocalDateTime(date, "yyyy-MM-dd HH:mm:ss");
        LocalDateTime d2 = cn.oyzh.common.date.DateUtil.parseLocalDateTime(date, "yyyy-MM-dd HH:mm:ss");
        System.out.println(d1);
        System.out.println(d2);
    }
}
