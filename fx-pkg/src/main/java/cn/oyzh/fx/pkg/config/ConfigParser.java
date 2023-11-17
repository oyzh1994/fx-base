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
     * 全局配置
     */
    @Getter
    private GlobalConfig globalConfig;

    /**
     * 平台配置
     */
    private final Map<String, PlatformConfig> platformConfigs = new HashMap<>();

    /**
     * 加载配置
     *
     * @param globalPath    全局配置路径
     * @param platformPaths 平台配置路径
     */
    public void loadConfig(String globalPath, String... platformPaths) {
        this.globalConfig = GlobalConfig.loadConfig(globalPath);
        for (String platformPath : platformPaths) {
            PlatformConfig platformConfig = PlatformConfig.loadConfig(platformPath);
            platformConfigs.put(platformConfig.getPackageConfig().getPlatform(), platformConfig);
        }
    }

    /**
     * 获取交叉的jre裁剪配置
     *
     * @param platform 平台
     * @return jre裁剪配置
     */
    public JreClipConfig getCrossJreClipConfig(String platform) {
        PlatformConfig config = this.platformConfigs.get(platform);
        JreClipConfig jreClipConfig = config.getJreClipConfig();
        if (jreClipConfig.getSrc() == null) {
            jreClipConfig.setSrc(this.getJrePath(platform));
        }
        if (jreClipConfig.getDest() == null) {
            jreClipConfig.setDest(this.getJreClipPath(platform));
        }
        return jreClipConfig;
    }

    /**
     * 获取交叉的jar裁剪配置
     *
     * @param platform 平台
     * @return jar裁剪配置
     */
    public JarClipConfig getCrossJarClipConfig(String platform) {
        PlatformConfig config = this.platformConfigs.get(platform);
        JarClipConfig jarClipConfig = config.getJarClipConfig();
        String src = this.globalConfig.getJarPath();
        if (jarClipConfig.getSrc() == null) {
            jarClipConfig.setSrc(src);
        }
        if (jarClipConfig.getDest() == null) {
            jarClipConfig.setDest(this.getJarClipPath(platform, src));
        }
        return jarClipConfig;
    }

    /**
     * 获取交叉的jlink配置
     *
     * @param platform 平台
     * @return jlink配置
     */
    public JLinkConfig getCrossJLinkConfig(String platform) {
        PlatformConfig config = this.platformConfigs.get(platform);
        JLinkConfig jLinkConfig = config.getJLinkConfig();
        if (jLinkConfig.getOutput() == null) {
            jLinkConfig.setOutput(this.getJrePath(platform));
        }
        return jLinkConfig;
    }

    /**
     * 获取交叉的平台配置
     *
     * @param platform 平台
     * @return 平台配置
     */
    public PlatformConfig getCrossPlatformConfig(String platform) {
        PlatformConfig config = this.platformConfigs.get(platform);
        PlatformConfig config1 = new PlatformConfig();
        config1.setEnable(config.isEnable());
        config1.setJdkPath(config.getJdkPath());
        config1.setJLinkConfig(this.getCrossJLinkConfig(platform));
        config1.setJarClipConfig(this.getCrossJarClipConfig(platform));
        config1.setJreClipConfig(this.getCrossJreClipConfig(platform));
        config1.setPackageConfig(this.getCrossPackageConfig(platform));
        return config1;
    }

    /**
     * 获取交叉的打包配置
     *
     * @param platform 平台
     * @return 打包配置
     */
    public PackageConfig getCrossPackageConfig(String platform) {
        PlatformConfig config = this.platformConfigs.get(platform);
        PackageConfig packageConfig = config.getPackageConfig();
        packageConfig.setPlatform(platform);
        String src = this.globalConfig.getJarPath();
        if (packageConfig.getJrePath() == null) {
            packageConfig.setJarPath(src);
        }
        if (packageConfig.getDestPath() == null) {
            packageConfig.setDestPath(this.globalConfig.getDestPath());
        }
        if (packageConfig.getVersion() == null) {
            packageConfig.setVersion(this.globalConfig.getVersion());
        }
        if (packageConfig.getVendor() == null) {
            packageConfig.setVendor(this.globalConfig.getVendor());
        }
        if (packageConfig.getExecutable() == null) {
            packageConfig.setExecutable(this.globalConfig.getExecutable());
        }
        if (packageConfig.getAppName() == null) {
            packageConfig.setAppName(this.globalConfig.getAppName());
        }
        if (packageConfig.getDesc() == null) {
            packageConfig.setDesc(this.globalConfig.getDesc());
        }
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
        return this.platformConfigs.keySet();
    }
}
