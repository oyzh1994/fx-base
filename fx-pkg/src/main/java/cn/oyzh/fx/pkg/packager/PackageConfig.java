package cn.oyzh.fx.pkg.packager;

import cn.oyzh.fx.common.util.FileNameUtil;
import cn.oyzh.fx.pkg.config.BaseConfig;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;

/**
 * 打包配置
 *
 * @author oyzh
 * @since 2023/11/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PackageConfig extends BaseConfig {

    /**
     * 程序描述
     */
    private String desc;

    /**
     * 作者
     */
    private String vendor;

    /**
     * 程序名称
     */
    private String appName;

    /**
     * 程序logo
     */
    private String appIcon;

    /**
     * 平台
     */
    private String platform;

    /**
     * 主jar路径
     */
    private String jarPath;

    /**
     * jre路径
     */
    private String jrePath;

    /**
     * 运行的主类
     */
    private String mainClass;

    /**
     * 目标路径
     */
    private String destPath;

    /**
     * 版本号
     */
    private String version;

    /**
     * 可执行程序的名称
     */
    private String executable;

    /**
     * windows
     * app-image|exe|msi
     * <p>
     * linux
     * app-image|rpm|deb
     * <p>
     * mac
     * app-image
     */
    private String type = "app-image";

    /**
     * 压缩类型
     * <p>
     * zip|tar.gz
     */
    private String compressType;

    /**
     * 程序唯一标识符(仅macos)
     */
    private String identifier;

    /**
     * 获取目标文件
     *
     * @return 目标文件
     */
    public File getDestFile() {
        if (this.destPath == null) {
            return null;
        }
        return new File(this.destPath);
    }

    /**
     * 获取app目标目录
     *
     * @return app目标目录
     */
    public String getAppDest() {
        return FileNameUtil.concat(this.destPath, this.appName);
    }

    /**
     * 获取icon文件
     *
     * @return icon文件
     */
    public File getIconFile() {
        if (this.appIcon == null) {
            return null;
        }
        return new File(this.appIcon);
    }

    /**
     * 获取主jar文件
     *
     * @return 主jar文件
     */
    public File getJarFile() {
        if (this.jarPath == null) {
            return null;
        }
        return new File(this.jarPath);
    }

    @Override
    public void parseConfig(JSONObject object) {
        super.parseConfig(object);
        String identifier = object.getString("identifier");
        if (identifier != null) {
            this.identifier = identifier;
        }
        String type = object.getString("type");
        if (type != null) {
            this.type = type;
        }
        String compressType = object.getString("compressType");
        if (compressType != null) {
            this.compressType = compressType;
        }
        String version = object.getString("version");
        if (version != null) {
            this.version = version;
        }
        String executable = object.getString("executable");
        if (executable != null) {
            this.executable = executable;
        }
        String destPath = object.getString("destPath");
        if (destPath != null) {
            this.destPath = destPath;
        }
        String mainClass = object.getString("mainClass");
        if (mainClass != null) {
            this.mainClass = mainClass;
        }
        String appIcon = object.getString("appIcon");
        if (appIcon != null) {
            this.appIcon = appIcon;
        }
        String jarPath = object.getString("jarPath");
        if (jarPath != null) {
            this.jarPath = jarPath;
        }
        String jrePath = object.getString("jrePath");
        if (jrePath != null) {
            this.jrePath = jrePath;
        }
        String appName = object.getString("appName");
        if (appName != null) {
            this.appName = appName;
        }
        String vendor = object.getString("vendor");
        if (vendor != null) {
            this.vendor = vendor;
        }
        String desc = object.getString("desc");
        if (desc != null) {
            this.desc = desc;
        }
    }

    @Override
    public PackageConfig cross(Object o) {
        if (o instanceof PackageConfig config) {
            PackageConfig config1 = new PackageConfig();
            config1.setEnable(config.isEnable());
            if (config.desc != null) {
                config1.desc = config.desc;
            } else {
                config1.desc = this.desc;
            }
            if (config.vendor != null) {
                config1.vendor = config.vendor;
            } else {
                config1.vendor = this.vendor;
            }
            if (config.appName != null) {
                config1.appName = config.appName;
            } else {
                config1.appName = this.appName;
            }
            if (config.jarPath != null) {
                config1.jarPath = config.jarPath;
            } else {
                config1.jarPath = this.jarPath;
            }
            if (config.jrePath != null) {
                config1.jrePath = config.jrePath;
            } else {
                config1.jrePath = this.jrePath;
            }
            if (config.appIcon != null) {
                config1.appIcon = config.appIcon;
            } else {
                config1.appIcon = this.appIcon;
            }
            if (config.mainClass != null) {
                config1.mainClass = config.mainClass;
            } else {
                config1.mainClass = this.mainClass;
            }
            if (config.destPath != null) {
                config1.destPath = config.destPath;
            } else {
                config1.destPath = this.destPath;
            }
            if (config.executable != null) {
                config1.executable = config.executable;
            } else {
                config1.executable = this.executable;
            }
            if (config.version != null) {
                config1.version = config.version;
            } else {
                config1.version = this.version;
            }
            if (config.compressType != null) {
                config1.compressType = config.compressType;
            } else {
                config1.compressType = this.compressType;
            }
            if (config.type != null) {
                config1.type = config.type;
            } else {
                config1.type = this.type;
            }
            if (config.identifier != null) {
                config1.identifier = config.identifier;
            } else {
                config1.identifier = this.identifier;
            }
            return config1;
        }
        return this;
    }
}
