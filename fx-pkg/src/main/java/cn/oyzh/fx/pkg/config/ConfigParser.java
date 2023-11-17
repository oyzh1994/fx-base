package cn.oyzh.fx.pkg.config;

import cn.oyzh.fx.common.util.FileNameUtil;
import cn.oyzh.fx.pkg.clip.clipper.JarClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JreClipConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.packager.PackageConfig;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 配置解析器
 *
 * @author oyzh
 * @since 2023/11/14
 */
public class ConfigParser {

    /**
     * 平台配置
     */
    private final Map<String, PlatformConfig> platformConfig = new HashMap<>();

    /**
     * 全局配置
     */
    @Getter
    private final GlobalConfig globalConfig = new GlobalConfig();

    /**
     * 全局打包配置
     */
    @Getter
    private final PackageConfig globalPackageConfig = new PackageConfig();

    /**
     * 全局jlink配置
     */
    @Getter
    private final JLinkConfig globalJLinkConfig = new JLinkConfig();

    /**
     * 全局jar裁剪配置
     */
    @Getter
    private final JarClipConfig globalJarClipConfig = new JarClipConfig();

    /**
     * 全局jre裁剪配置
     */
    @Getter
    private final JreClipConfig globalJreClipConfig = new JreClipConfig();

    /**
     * 解析配置
     *
     * @param configPath 配置路径
     */
    public void parseConfig(String configPath) {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getResourceAsStream(configPath);
        JSONObject obj = new JSONObject(yaml.load(inputStream));
        if (obj.containsKey("global")) {
            JSONObject object = obj.getJSONObject("global");
            this.globalConfig.parseConfig(object);
        }
        if (obj.containsKey("package")) {
            JSONObject object = obj.getJSONObject("package");
            this.globalPackageConfig.parseConfig(object);
        }
        if (obj.containsKey("jlink")) {
            JSONObject object = obj.getJSONObject("jlink");
            this.globalJLinkConfig.parseConfig(object);
        }
        if (obj.containsKey("jre_clip")) {
            JSONObject object = obj.getJSONObject("jre_clip");
            this.globalJreClipConfig.parseConfig(object);
        }
        if (obj.containsKey("jar_clip")) {
            JSONObject object = obj.getJSONObject("jar_clip");
            this.globalJarClipConfig.parseConfig(object);
        }
        if (obj.containsKey("platform")) {
            JSONObject platformObj = obj.getJSONObject("platform");
            for (String platform : platformObj.keySet()) {
                JSONObject object1 = platformObj.getJSONObject(platform);
                PlatformConfig config = new PlatformConfig();
                config.parseConfig(object1);
                this.platformConfig.put(platform, config);
            }
        }
    }

    /**
     * 获取交叉的jre裁剪配置
     *
     * @param platform 平台
     * @return jre裁剪配置
     */
    public JreClipConfig getCrossJreClipConfig(String platform) {
        PlatformConfig config = this.platformConfig.get(platform);
        JreClipConfig jreClipConfig = this.globalJreClipConfig.cross(config.getJreClipConfig());
        if (jreClipConfig.getSrc() == null) {
            jreClipConfig.setSrc(this.getJrePath(platform));
        }
        jreClipConfig.setDest(this.getJreClipPath(platform));
        return jreClipConfig;
    }

    /**
     * 获取交叉的jar裁剪配置
     *
     * @param platform 平台
     * @return jar裁剪配置
     */
    public JarClipConfig getCrossJarClipConfig(String platform) {
        PlatformConfig config = this.platformConfig.get(platform);
        JarClipConfig jarClipConfig = this.globalJarClipConfig.cross(config.getJarClipConfig());
        String src = this.globalConfig.getJarPath();
        jarClipConfig.setSrc(src);
        jarClipConfig.setDest(this.getJarClipPath(platform, src));
        return jarClipConfig;
    }

    /**
     * 获取交叉的jlink配置
     *
     * @param platform 平台
     * @return jlink配置
     */
    public JLinkConfig getCrossJLinkConfig(String platform) {
        PlatformConfig config = this.platformConfig.get(platform);
        JLinkConfig jLinkConfig = this.globalJLinkConfig.cross(config.getJLinkConfig());
        jLinkConfig.setOutput(this.getJrePath(platform));
        return jLinkConfig;
    }

    /**
     * 获取交叉的平台配置
     *
     * @param platform 平台
     * @return 平台配置
     */
    public PlatformConfig getCrossPlatformConfig(String platform) {
        PlatformConfig config = this.platformConfig.get(platform);
        PlatformConfig config1 = new PlatformConfig();
        config1.setJarClipConfig(this.getCrossJarClipConfig(platform));
        config1.setJreClipConfig(this.getCrossJreClipConfig(platform));
        config1.setPackageConfig(this.getCrossPackageConfig(platform));
        config1.setJLinkConfig(this.getCrossJLinkConfig(platform));
        config1.setEnable(config.isEnable());
        return config1;
    }

    /**
     * 获取交叉的打包配置
     *
     * @param platform 平台
     * @return 打包配置
     */
    public PackageConfig getCrossPackageConfig(String platform) {
        PlatformConfig config = this.platformConfig.get(platform);
        PackageConfig packageConfig = this.globalPackageConfig.cross(config.getPackageConfig());
        String src = this.globalConfig.getJarPath();
        packageConfig.setJarPath(src);
        packageConfig.setPlatform(platform);
        packageConfig.setDestPath(this.globalConfig.getDestPath());
        return packageConfig;
    }

    public String getJrePath(String platform) {
        return FileNameUtil.concat(this.globalConfig.getDestPath(), platform + "_jre");
    }

    public String getJreClipPath(String platform) {
        return FileNameUtil.concat(this.globalConfig.getDestPath(), platform + "_jre_clip");
    }

    public String getJarClipPath(String platform, String src) {
        return src.replaceFirst(".jar", "_" + platform + "_clip.jar");
    }

    public Set<String> getPlatforms() {
        return this.platformConfig.keySet();
    }
}
