package cn.oyzh.fx.pkg.packr;

import cn.hutool.json.JSONObject;
import cn.oyzh.fx.pkg.ConfigParser;
import com.badlogicgames.packr.PackrConfig;

import java.io.File;
import java.util.ArrayList;

/**
 * packr配置解析器
 *
 * @author oyzh
 * @since 2024/6/17
 */
public class PackrConfigParser implements ConfigParser<PackrConfig> {

    @Override
    public PackrConfig parse(JSONObject object) {
        PackrConfig packrConfig = new PackrConfig();
        // 基础配置
        packrConfig.jdk = object.getStr("jdk");
        packrConfig.mainClass = object.getStr("mainClass");
        packrConfig.executable = object.getStr("executable");
        if (object.containsKey("outDir")) {
            packrConfig.outDir = new File(object.getStr("outDir"));
        }
        if (object.containsKey("platform")) {
            packrConfig.platform = PackrConfig.Platform.byDesc(object.getStr("platform"));
        }
        if (object.containsKey("iconResource")) {
            packrConfig.iconResource = new File(object.getStr("iconResource"));
        }
        packrConfig.bundleIdentifier = object.getStr("bundleIdentifier");
        packrConfig.verbose = object.getBool("verbose", false);
        packrConfig.classpath = object.getJSONArray("classpath").toList(String.class);
        packrConfig.useZgcIfSupportedOs = object.getBool("useZgcIfSupportedOs", true);
        if (object.containsKey("vmArgs")) {
            packrConfig.vmArgs = object.getJSONArray("vmArgs").toList(String.class);
        } else {
            packrConfig.vmArgs = new ArrayList<>();
        }
        return packrConfig;
    }

    public static PackrConfig parseConfig(JSONObject object) {
        return new PackrConfigParser().parse(object);
    }

    public static PackrConfig parseConfig(String configFile) {
        return new PackrConfigParser().parse(configFile);
    }
}
