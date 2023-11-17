package cn.oyzh.fx.common.util;

import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * 文件名称工具类
 *
 * @author oyzh
 * @since 2023/11/17
 */
@UtilityClass
public class FileNameUtil {

    /**
     * 追加路径
     *
     * @param paths 路径列表
     * @return 新路径
     */
    public static String concat(String... paths) {
        StringBuilder builder = new StringBuilder();
        for (String path : paths) {
            builder.append(path);
            if (!path.endsWith(File.separator)) {
                builder.append(File.separator);
            }
        }
        return builder.toString();
    }
}
