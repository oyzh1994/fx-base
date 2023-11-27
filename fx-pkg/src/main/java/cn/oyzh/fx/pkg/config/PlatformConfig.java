package cn.oyzh.fx.pkg.config;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.oyzh.fx.pkg.clip.clipper.JarClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JreClipConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.packager.PackageConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2023/11/17
 */
@Getter
@Setter
public class PlatformConfig extends BaseConfig {

    /**
     * jdk路径
     */
    private String jdkPath;

    /**
     * jlink配置
     */
    private JLinkConfig jLinkConfig;

    /**
     * jar裁剪配置
     */
    private JarClipConfig jarClipConfig;

    /**
     * jre裁剪配置
     */
    private JreClipConfig jreClipConfig;

    /**
     * 打包配置
     */
    private PackageConfig packageConfig;

    @Override
    public void parseConfig(JSONObject object1) {
        super.parseConfig(object1);
        this.jdkPath = object1.getStr("jdkPath");
        if (object1.containsKey("package")) {
            JSONObject object = object1.getJSONObject("package");
            this.packageConfig = new PackageConfig();
            this.packageConfig.parseConfig(object);
        }
        if (object1.containsKey("jlink")) {
            JSONObject object = object1.getJSONObject("jlink");
            this.jLinkConfig = new JLinkConfig();
            this.jLinkConfig.parseConfig(object);
        }
        if (object1.containsKey("jre_clip")) {
            JSONObject object = object1.getJSONObject("jre_clip");
            this.jreClipConfig = new JreClipConfig();
            this.jreClipConfig.parseConfig(object);
        }
        if (object1.containsKey("jar_clip")) {
            JSONObject object = object1.getJSONObject("jar_clip");
            this.jarClipConfig = new JarClipConfig();
            this.jarClipConfig.parseConfig(object);
        }
    }

    @Override
    public PlatformConfig clone() {
        PlatformConfig config = new PlatformConfig();
        config.jdkPath = this.jdkPath;
        config.jLinkConfig = this.jLinkConfig;
        config.packageConfig = this.packageConfig;
        config.jarClipConfig = this.jarClipConfig;
        config.jreClipConfig = this.jreClipConfig;
        return config;
    }

    @Override
    public PlatformConfig cross(Object o) {
        PlatformConfig config1 = this.clone();
        if (o instanceof PlatformConfig config) {
            config1.enable = config.enable;
            if (config.jLinkConfig != null) {
                config1.jLinkConfig = config.jLinkConfig;
            }
            if (config.jreClipConfig != null) {
                config1.jreClipConfig = config.jreClipConfig;
            }
            if (config.jarClipConfig != null) {
                config1.jarClipConfig = config.jarClipConfig;
            }
            if (config.packageConfig != null) {
                config1.packageConfig = config.packageConfig;
            }
            if (config.jdkPath != null) {
                config1.jdkPath = config.jdkPath;
            }
        }
        return config1;
    }

    /**
     * 加载配置
     *
     * @param configPath 配置路径
     * @return PlatformConfig
     */
    public static PlatformConfig loadConfig(String configPath) {
        String text = ResourceUtil.readUtf8Str(configPath);
        JSONObject object = JSONUtil.parseObj(text);
        PlatformConfig config = new PlatformConfig();
        config.parseConfig(object);
        return config;
    }

    public String getPlatform() {
        return this.packageConfig.getPlatform();
    }

    public void setPlatform(String platform) {
        this.packageConfig.setPlatform(platform);
    }
}
