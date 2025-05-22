package cn.oyzh.fx.pkg.jpackage;

import cn.oyzh.fx.pkg.ConfigParser;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;

/**
 * JPackage配置
 *
 * @author oyzh
 * @since 2023/3/8
 */
public class JPackageConfigParser implements ConfigParser<JPackageConfig> {

    @Override
    public JPackageConfig parse(JSONObject object) {
        JPackageConfig config = new JPackageConfig();
        String name = object.getString("name");
        if (name != null) {
            config.setName(name);
        }
        String type = object.getString("type");
        if (type != null) {
            config.setType(type);
        }
        String appVersion = object.getString("appVersion");
        if (appVersion != null) {
            config.setAppVersion(appVersion);
        }
        String mainJar = object.getString("mainJar");
        if (mainJar != null) {
            config.setMainJar(mainJar);
        }
        String runtimeImage = object.getString("runtimeImage");
        if (runtimeImage != null) {
            config.setRuntimeImage(runtimeImage);
        }
        String icon = object.getString("icon");
        if (icon != null) {
            config.setIcon(icon);
        }
        String input = object.getString("input");
        if (input != null) {
            config.setInput(input);
        }
        String dest = object.getString("dest");
        if (dest != null) {
            config.setDest(dest);
        }
        String vendor = object.getString("vendor");
        if (vendor != null) {
            config.setVendor(vendor);
        }
        Boolean verbose = object.getBoolean("verbose");
        if (vendor != null) {
            config.setVerbose(verbose);
        }
        List<String> javaOptions = object.getList("java-options", String.class);
        if (javaOptions != null) {
            config.setJavaOptions(javaOptions);
        }
        Boolean winMenu = object.getBoolean("win-menu");
        if (winMenu != null) {
            config.setWinMenu(winMenu);
        }
        Boolean winShortcut = object.getBoolean("win-shortcut");
        if (winShortcut != null) {
            config.setWinShortcut(winShortcut);
        }
        Boolean winDirChooser = object.getBoolean("win-dir-chooser");
        if (winDirChooser != null) {
            config.setWinDirChooser(winDirChooser);
        }
        String macPackageIdentifier = object.getString("mac-package-identifier");
        if (macPackageIdentifier != null) {
            config.setMacPackageIdentifier(macPackageIdentifier);
        }
        String description = object.getString("description");
        if (description != null) {
            config.setDescription(description);
        }
        return config;
    }

    public static JPackageConfig parseConfig(JSONObject object) {
        return new JPackageConfigParser().parse(object);
    }

    public static JPackageConfig parseConfig(String configFile) {
        return new JPackageConfigParser().parse(configFile);
    }
}
