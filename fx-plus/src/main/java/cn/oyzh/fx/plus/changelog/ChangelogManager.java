package cn.oyzh.fx.plus.changelog;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.ResourceUtil;
import lombok.experimental.UtilityClass;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 更新日志管理器
 *
 * @author oyzh
 * @since 2024/04/07
 */
@UtilityClass
public class ChangelogManager {

    /**
     * 加载更新日志
     *
     * @return 更新日志列表
     */
    public static List<Changelog> load() {
        return load("/changelog.json");
    }

    /**
     * 加载更新日志
     *
     * @param url 文件地址
     * @return 更新日志列表
     */
    public static List<Changelog> load(String url) {
        URL url1 = ResourceUtil.getResource(url);
        String json = FileUtil.readString(url1, StandardCharsets.UTF_8);
        return JSONUtil.toBeanList(json, Changelog.class);
    }
}
