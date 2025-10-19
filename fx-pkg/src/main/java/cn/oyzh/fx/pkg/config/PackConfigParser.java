package cn.oyzh.fx.pkg.config;

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
import cn.oyzh.fx.pkg.packr.PackrConfigParser;
import com.alibaba.fastjson2.JSONObject;
import com.badlogicgames.packr.PackrConfig;

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
        config.setDest(object.getString("dest"));
        config.setAppName(object.getString("appName"));
        config.setAppIcon(object.getString("appIcon"));
        config.setAppVersion(object.getString("appVersion"));
        config.setMainJar(object.getString("mainJar"));
        config.setJdkPath(object.getString("jdkPath"));
        config.setJrePath(object.getString("jrePath"));
        config.setPlatform(object.getString("platform"));
        config.setPackMode(object.getString("packMode"));
        config.setBuildType(object.getString("buildType"));
        config.setAppImageRuntime(object.getString("appImageRuntime"));

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
            config.setjLinkConfig(jLinkConfig);
        }

        if (object.containsKey("jdeps")) {
            JDepsConfig jDepsConfig = JDepsConfigParser.parseConfig(object.getJSONObject("jdeps"));
            config.setjDepsConfig(jDepsConfig);
        }

        if (object.containsKey("compress")) {
            CompressConfig compressConfig = CompressConfigParser.parseConfig(object.getJSONObject("compress"));
            config.setCompressConfig(compressConfig);
        }

        if (object.containsKey("jpackage")) {
            JPackageConfig jPackageConfig = JPackageConfigParser.parseConfig(object.getJSONObject("jpackage"));
            config.setjPackageConfig(jPackageConfig);
        }

        return config;
    }

    public static PackConfig parseConfig(String configFile) {
        return new PackConfigParser().parse(configFile);
    }
}
