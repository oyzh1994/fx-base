package cn.oyzh.fx.pkg.config;

import cn.hutool.json.JSONObject;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.comporess.CompressConfig;
import cn.oyzh.fx.pkg.comporess.CompressConfigParser;
import cn.oyzh.fx.pkg.jar.JarConfig;
import cn.oyzh.fx.pkg.jar.JarConfigParser;
import cn.oyzh.fx.pkg.jdeps.JDepsConfig;
import cn.oyzh.fx.pkg.jdeps.JDepsConfigParser;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfigParser;
import cn.oyzh.fx.pkg.jpackage.JPackageConfig;
import cn.oyzh.fx.pkg.jpackage.JPackageConfigParser;
import cn.oyzh.fx.pkg.jre.JreConfig;
import cn.oyzh.fx.pkg.jre.JreConfigParser;
import cn.oyzh.fx.pkg.packr.PackrConfig;
import cn.oyzh.fx.pkg.packr.PackrConfigParser;

/**
 * 打包配置解析器
 *
 * @author oyzh
 * @since 2024/6/17
 */
public class PackConfigParser implements ConfigParser<PackConfig> {

    @Override
    public PackConfig parse(JSONObject object) {
        PackConfig config = new PackConfig();

        // 扩展配置
        config.setDest(object.getStr("dest"));
        config.setAppName(object.getStr("appName"));
        config.setAppIcon(object.getStr("appIcon"));
        config.setAppVersion(object.getStr("appVersion"));
        config.setMainJar(object.getStr("mainJar"));
        config.setJdkPath(object.getStr("jdkPath"));
        config.setJrePath(object.getStr("jrePath"));
        config.setPlatform(object.getStr("platform"));
        config.setPackMode(object.getStr("packMode"));
        config.setBuildType(object.getStr("buildType"));

        if (object.containsKey("jre")) {
            JreConfig jreConfig = JreConfigParser.parseConfig(object.getJSONObject("jre"));
            config.setJreConfig(jreConfig);
        }

        if (object.containsKey("jar")) {
            JarConfig jarConfig = JarConfigParser.parseConfig(object.getJSONObject("jar"));
            config.setJarConfig(jarConfig);
        }

        if (object.containsKey("packr")) {
            PackrConfig packrConfig = PackrConfigParser.parseConfig(object.getJSONObject("packr"));
            config.setPackrConfig(packrConfig);
        }

        if (object.containsKey("jlink")) {
            JLinkConfig jLinkConfig = JLinkConfigParser.parseConfig(object.getJSONObject("jlink"));
            config.setJLinkConfig(jLinkConfig);
        }

        if (object.containsKey("jdeps")) {
            JDepsConfig jDepsConfig = JDepsConfigParser.parseConfig(object.getJSONObject("jdeps"));
            config.setJDepsConfig(jDepsConfig);
        }

        if (object.containsKey("compress")) {
            CompressConfig compressConfig = CompressConfigParser.parseConfig(object.getJSONObject("compress"));
            config.setCompressConfig(compressConfig);
        }

        if (object.containsKey("jpackage")) {
            JPackageConfig jPackageConfig = JPackageConfigParser.parseConfig(object.getJSONObject("jpackage"));
            config.setJPackageConfig(jPackageConfig);
        }

        return config;
    }

    public static PackConfig parseConfig(String configFile) {
        return new PackConfigParser().parse(configFile);
    }
}
