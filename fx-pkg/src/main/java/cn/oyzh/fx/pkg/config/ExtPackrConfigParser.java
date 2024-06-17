package cn.oyzh.fx.pkg.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.packr.Platform;

import java.io.File;
import java.util.ArrayList;

/**
 * @author oyzh
 * @since 2024/6/17
 */
public class ExtPackrConfigParser implements ConfigParser<ExtPackrConfig> {

    @Override
    public ExtPackrConfig parse(String configFile) {
        JSONObject object = JSONUtil.parseObj(FileUtil.readUtf8String(configFile));

        ExtPackrConfig config = new ExtPackrConfig();

        // 扩展配置
        config.setMainJar(object.getStr("mainJar"));
        config.setVersion(object.getStr("version"));
        config.setBuildType(object.getStr("buildType"));
        config.setCompressType(object.getStr("compressType"));

        // 基础配置
        config.jdk = object.getStr("jdk");
        config.mainClass = object.getStr("mainClass");
        config.executable = object.getStr("executable");
        config.outDir = new File(object.getStr("outDir"));
        config.bundleIdentifier = object.getStr("bundleIdentifier");
        config.iconResource = new File(object.getStr("iconResource"));
        config.platform = Platform.byDesc(object.getStr("platform"));
        config.verbose = object.getBool("verbose", false);
        config.classpath = object.getJSONArray("classpath").toList(String.class);
        config.useZgcIfSupportedOs = object.getBool("useZgcIfSupportedOs", true);
        if (object.containsKey("vmArgs")) {
            config.vmArgs = object.getJSONArray("vmArgs").toList(String.class);
        } else {
            config.vmArgs = new ArrayList<>();
        }
        return config;
    }

    public static ExtPackrConfig parseConfig(String configFile) {
        return new ExtPackrConfigParser().parse(configFile);
    }
}
