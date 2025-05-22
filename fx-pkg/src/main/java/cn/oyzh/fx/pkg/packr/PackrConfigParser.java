package cn.oyzh.fx.pkg.packr;

import cn.oyzh.fx.pkg.ConfigParser;
import com.alibaba.fastjson2.JSONObject;
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
        packrConfig.jdk = object.getString("jdk");
        packrConfig.mainClass = object.getString("mainClass");
        packrConfig.executable = object.getString("executable");
        if (object.containsKey("outDir")) {
            packrConfig.outDir = new File(object.getString("outDir"));
        }
        if (object.containsKey("platform")) {
            packrConfig.platform = PackrConfig.Platform.byDesc(object.getString("platform"));
        }
        if (object.containsKey("iconResource")) {
            packrConfig.iconResource = new File(object.getString("iconResource"));
        }
        packrConfig.bundleIdentifier = object.getString("bundleIdentifier");
        packrConfig.verbose = object.getBooleanValue("verbose", false);
        packrConfig.classpath = object.getJSONArray("classpath").toList(String.class);
        packrConfig.useZgcIfSupportedOs = object.getBooleanValue("useZgcIfSupportedOs", true);
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
