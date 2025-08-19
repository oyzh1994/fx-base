package cn.oyzh.fx.plus.util;

import cn.oyzh.common.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 样式工具类
 *
 * @author oyzh
 * @since 2023/4/4
 */
public class StyleUtil {

    /**
     * 连接样式文件
     *
     * @param cssList 样式列表
     * @return 连接后的样式地址
     */
    public static String join(String... cssList) {
        if (ArrayUtil.isNotEmpty(cssList)) {
            StringBuilder builder = new StringBuilder();
            for (String s : cssList) {
                builder.append(";").append(s);
            }
            return builder.substring(1);
        }
        return "";
    }

    /**
     * 分割样式文件
     *
     * @param cssList 样式列表
     * @return 分割后的样式列表
     */
    public static List<String> split(String... cssList) {
        if (ArrayUtil.isNotEmpty(cssList)) {
            List<String> list = new ArrayList<>(4);
            for (String s : cssList) {
                if (s.contains(";")) {
                    Collections.addAll(list, s.split(";"));
                } else {
                    list.add(s);
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
