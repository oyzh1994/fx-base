package cn.oyzh.fx.pkg.jpackage;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.oyzh.fx.pkg.config.BaseConfig;
import cn.oyzh.fx.pkg.packager.PackageConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JPackage配置
 *
 * @author oyzh
 * @since 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JPackageConfig extends BaseConfig {

    /**
     * 程序名
     */
    private String name;

    /**
     * 打包类型
     */
    private String type;

    /**
     * 目标目录
     */
    private String dest;

    /**
     * 输入目录
     */
    private String input;

    /**
     * 图标文件
     */
    private String icon;

    /**
     * 作者
     */
    private String vendor;

    /**
     * 主jar
     */
    private String mainJar;

    /**
     * app版本
     */
    private String appVersion;

    /**
     * 程序描述
     */
    private String description;

    /**
     * 运行期jre目录
     */
    private String runtimeImage;

    /**
     * 过程信息
     */
    private boolean verbose;

    @Override
    public void parseConfig(JSONObject object) {
        super.parseConfig(object);
        String name = object.getStr("name");
        if (name != null) {
            this.name = name;
        }
        String type = object.getStr("type");
        if (type != null) {
            this.type = type;
        }
        String appVersion = object.getStr("appVersion");
        if (appVersion != null) {
            this.appVersion = appVersion;
        }
        String mainJar = object.getStr("mainJar");
        if (mainJar != null) {
            this.mainJar = mainJar;
        }
        String runtimeImage = object.getStr("runtimeImage");
        if (runtimeImage != null) {
            this.runtimeImage = runtimeImage;
        }
        String icon = object.getStr("icon");
        if (icon != null) {
            this.icon = icon;
        }
        String input = object.getStr("input");
        if (input != null) {
            this.input = input;
        }
        String dest = object.getStr("dest");
        if (dest != null) {
            this.dest = dest;
        }
        String vendor = object.getStr("vendor");
        if (vendor != null) {
            this.vendor = vendor;
        }
        Boolean verbose = object.getBool("verbose");
        if (vendor != null) {
            this.verbose = verbose;
        }
        String description = object.getStr("description");
        if (description != null) {
            this.description = description;
        }
    }

    @Override
    public JPackageConfig cross(Object o) {
        if (o instanceof JPackageConfig config) {
            JPackageConfig config1 = new JPackageConfig();
            config1.setEnable(config.isEnable());
            config1.verbose = config.verbose;
            if (config.description != null) {
                config1.description = config.description;
            } else {
                config1.description = this.description;
            }
            if (config.name != null) {
                config1.name = config.name;
            } else {
                config1.name = this.name;
            }
            if (config.mainJar != null) {
                config1.mainJar = config.mainJar;
            } else {
                config1.mainJar = this.mainJar;
            }
            if (config.icon != null) {
                config1.icon = config.icon;
            } else {
                config1.icon = this.icon;
            }
            if (config.input != null) {
                config1.input = config.input;
            } else {
                config1.input = this.input;
            }
            if (config.dest != null) {
                config1.dest = config.dest;
            } else {
                config1.dest = this.dest;
            }
            if (config.runtimeImage != null) {
                config1.runtimeImage = config.runtimeImage;
            } else {
                config1.runtimeImage = this.runtimeImage;
            }
            if (config.vendor != null) {
                config1.vendor = config.vendor;
            } else {
                config1.vendor = this.vendor;
            }
            if (config.appVersion != null) {
                config1.appVersion = config.appVersion;
            } else {
                config1.appVersion = this.appVersion;
            }
            if (config.type != null) {
                config1.type = config.type;
            } else {
                config1.type = this.type;
            }
            return config1;
        }
        return this;
    }

    /**
     * 从打包配置生成配置
     *
     * @param config 打包配置
     * @return 配置对象
     */
    public static JPackageConfig from(PackageConfig config) {
        JPackageConfig packageConfig = new JPackageConfig();
        packageConfig.verbose = true;
        packageConfig.setType(config.getType());
        packageConfig.setName(config.getAppName());
        packageConfig.setIcon(config.getAppIcon());
        packageConfig.setDest(config.getDestPath());
        packageConfig.setVendor(config.getVendor());
        packageConfig.setDescription(config.getDesc());
        if (StrUtil.isNotBlank(config.getVersion())) {
            packageConfig.setAppVersion(config.getVersion().toLowerCase().replace("v", ""));
        }
        if (StrUtil.isNotBlank(config.getJrePath())) {
            packageConfig.setRuntimeImage(config.getJrePath());
        }
        return packageConfig;
    }
}
