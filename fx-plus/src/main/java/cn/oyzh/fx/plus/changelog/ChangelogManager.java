package cn.oyzh.fx.plus.changelog;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 更新日志管理器
 *
 * @author oyzh
 * @since 2024-04-07
 */

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
        return JSONUtil.toList(json, Changelog.class);
    }

//    /**
//     * 加载更新日志
//     *
//     * @param urls 文件地址
//     * @return 更新日志列表
//     */
//    public static List<Changelog> loadMDs(List<String> urls) {
//        List<Changelog> list = new ArrayList<>();
//        for (String url : urls) {
//            list.add(loadMD(url));
//        }
//        return list;
//    }
//
//    /**
//     * 加载更新日志
//     *
//     * @param url 文件地址
//     * @return 更新日志列表
//     */
//    public static Changelog loadMD(String url) {
//        URL url1 = ResourceUtil.getResource(url);
//        List<String> lines = FileUtil.readLines(url1, StandardCharsets.UTF_8);
//        Changelog changelog = new Changelog();
//        int type = -1;
//        for (String line : lines) {
//            if (StringUtil.isBlank(line)) {
//                continue;
//            }
//            if (line.startsWith("### 新特性")) {
//                changelog.setFeatures(new ArrayList<>());
//                type = 1;
//                continue;
//            }
//            if (line.startsWith("### 问题修复")) {
//                changelog.setBugfix(new ArrayList<>());
//                type = 2;
//                continue;
//            }
//            if (line.startsWith("### 设计优化")) {
//                changelog.setOptimize(new ArrayList<>());
//                type = 3;
//                continue;
//            }
//            if (type == 1) {
//                changelog.getFeatures().add(line);
//            } else if (type == 2) {
//                changelog.getBugfix().add(line);
//            } else if (type == 3) {
//                changelog.getOptimize().add(line);
//            }
//        }
//        return changelog;
//    }
}
