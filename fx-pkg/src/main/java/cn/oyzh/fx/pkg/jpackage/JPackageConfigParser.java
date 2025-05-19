package cn.oyzh.fx.pkg.jpackage;

import cn.hutool.json.JSONObject;
import cn.oyzh.fx.pkg.ConfigParser;

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
        String name = object.getStr("name");
        if (name != null) {
            config.setName(name);
        }
        String type = object.getStr("type");
        if (type != null) {
            config.setType(type);
        }
        String appVersion = object.getStr("appVersion");
        if (appVersion != null) {
            config.setAppVersion(appVersion);
        }
        String mainJar = object.getStr("mainJar");
        if (mainJar != null) {
            config.setMainJar(mainJar);
        }
        String runtimeImage = object.getStr("runtimeImage");
        if (runtimeImage != null) {
            config.setRuntimeImage(runtimeImage);
        }
        String icon = object.getStr("icon");
        if (icon != null) {
            config.setIcon(icon);
        }
        String input = object.getStr("input");
        if (input != null) {
            config.setInput(input);
        }
        String dest = object.getStr("dest");
        if (dest != null) {
            config.setDest(dest);
        }
        String vendor = object.getStr("vendor");
        if (vendor != null) {
            config.setVendor(vendor);
        }
        Boolean verbose = object.getBool("verbose");
        if (vendor != null) {
            config.setVerbose(verbose);
        }
        Boolean winMenu = object.getBool("win-menu");
        if (winMenu != null) {
            config.setWinMenu(winMenu);
        }
        Boolean winShortcut = object.getBool("win-shortcut");
        if (winShortcut != null) {
            config.setWinShortcut(winShortcut);
        }
        Boolean winDirChooser = object.getBool("win-dir-chooser");
        if (winDirChooser != null) {
            config.setWinDirChooser(winDirChooser);
        }
        String macPackageIdentifier = object.getStr("mac-package-identifier");
        if (macPackageIdentifier != null) {
            config.setMacPackageIdentifier(macPackageIdentifier);
        }
        String linuxPackageProperty = object.getStr("linux-package-property");
        if (linuxPackageProperty != null) {
            config.setLinuxPackageProperty(linuxPackageProperty);
        }
        String description = object.getStr("description");
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
