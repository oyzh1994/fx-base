package cn.oyzh.fx.common.file;

import cn.oyzh.fx.common.util.StringUtil;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class FileNameUtil {

    public static boolean isType(String name, String... types) {
        if (name == null || !name.contains(".") || types == null || types.length == 0) {
            return false;
        }
        String ext = name.substring(name.lastIndexOf(".") + 1);
        return StringUtil.equalsAnyIgnoreCase(ext, types);
    }
}
