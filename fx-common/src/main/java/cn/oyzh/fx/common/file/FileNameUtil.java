package cn.oyzh.fx.common.file;

import cn.oyzh.fx.common.util.StringUtil;
import lombok.NonNull;

import java.io.File;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class FileNameUtil {

    public static boolean isType(String name, String... types) {
        if (name == null || !name.contains(".") || types == null || types.length == 0) {
            return false;
        }
        String ext = extName(name);
        return StringUtil.equalsAnyIgnoreCase(ext, types);
    }

    public static String extName( String name) {
        if (name == null || !name.contains(".") ) {
            return "";
        }
        return  name.substring(name.lastIndexOf(".") + 1);
    }

    public static String extName( File file) {
       return file==null?null: extName(file.getName());
    }
}
