package cn.oyzh.fx.pkg.config;

import cn.oyzh.fx.common.util.FileNameUtil;
import cn.oyzh.fx.pkg.clip.clipper.JarClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JreClipConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.packager.PackageConfig;
import lombok.Getter;

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
            this.platformConfigs.put(platformConfig.getPlatform(), platformConfig);
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
        JreClipConfig jreClipConfig = this.globalConfig.getJreClipConfig();
        if (config.getJreClipConfig() != null) {
            JreClipConfig jreClipConfig1 = jreClipConfig.cross(config.getJreClipConfig());
            if (jreClipConfig1.getSrc() == null) {
                jreClipConfig1.setSrc(this.getJrePath(platform));
            }
            if (jreClipConfig1.getDest() == null) {
                jreClipConfig1.setDest(this.getJreClipPath(platform));
            }
            return jreClipConfig1;
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
        JarClipConfig jarClipConfig = this.globalConfig.getJarClipConfig();
        if (config.getJarClipConfig() != null) {
            JarClipConfig jarClipConfig1 = jarClipConfig.cross(config.getJarClipConfig());
            String src = this.globalConfig.getJarPath();
            if (jarClipConfig1.getSrc() == null) {
                jarClipConfig1.setSrc(src);
            }
            if (jarClipConfig1.getDest() == null) {
                jarClipConfig1.setDest(this.getJarClipPath(platform, src));
            }
            return jarClipConfig1;
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
        JLinkConfig jLinkConfig = this.globalConfig.getJLinkConfig();
        if (config.getJLinkConfig() != null) {
            JLinkConfig jLinkConfig1 = jLinkConfig.cross(config.getJLinkConfig());
            if (jLinkConfig1.getOutput() == null) {
                jLinkConfig1.setOutput(this.getJrePath(platform));
            }
            return jLinkConfig1;
        }
        return jLinkConfig;
    }

    /**
     * 获取交叉的打包配置
     *
     * @param platform 平台
     * @return 打包配置
     */
    public PackageConfig getCrossPackageConfig(String platform) {
        PlatformConfig config = this.platformConfigs.get(platform);
        PackageConfig packageConfig = this.globalConfig.getPackageConfig();
        packageConfig.setPlatform(platform);
        if (config.getPackageConfig() != null) {
            PackageConfig packageConfig1 = packageConfig.cross(config.getPackageConfig());
            String src = this.globalConfig.getJarPath();
            if (packageConfig1.getJrePath() == null) {
                packageConfig1.setJarPath(src);
            }
            if (packageConfig1.getDestPath() == null) {
                packageConfig1.setDestPath(this.globalConfig.getDestPath());
            }
            return packageConfig1;
        }
        return packageConfig;
    }

    /**
     * 获取交叉的平台配置
     *
     * @param platform 平台
     * @return 平台配置
     */
    public PlatformConfig getCrossPlatformConfig(String platform) {
        PlatformConfig config = this.platformConfigs.get(platform);
        if (config.getPlatform() == null) {
            config.setPlatform(platform);
        }
        if (config.getJdkPath() == null) {
            config.setJdkPath(this.globalConfig.getJdkPath());
        }
        PlatformConfig config1 = new PlatformConfig();
        config1.setJLinkConfig(this.getCrossJLinkConfig(platform));
        config1.setJarClipConfig(this.getCrossJarClipConfig(platform));
        config1.setJreClipConfig(this.getCrossJreClipConfig(platform));
        config1.setPackageConfig(this.getCrossPackageConfig(platform));
        return config.cross(config1);
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
