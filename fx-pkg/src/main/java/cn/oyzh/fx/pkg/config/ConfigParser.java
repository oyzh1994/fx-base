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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置解析器
 *
 * @author oyzh
 * @since 2023/11/14
 */
public class ConfigParser {

    /**
     * 平台列表
     */
    @Getter
    private final List<String> platforms = new ArrayList<>();

    /**
     * 平台打包配置
     */
    private final Map<String, PackageConfig> platformPackageConfig = new HashMap<>();

    /**
     * 平台jlink配置
     */
    private final Map<String, JLinkConfig> platformJlinkConfig = new HashMap<>();

    /**
     * 平台jar裁剪配置
     */
    private final Map<String, JarClipConfig> platformJarClipConfig = new HashMap<>();

    /**
     * 平台jre裁剪配置
     */
    private final Map<String, JreClipConfig> platformJreClipConfig = new HashMap<>();

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
            this.platforms.addAll(platformObj.keySet());
            for (String platform : platforms) {
                JSONObject object1 = platformObj.getJSONObject(platform);
                if (object1.containsKey("package")) {
                    JSONObject object = object1.getJSONObject("package");
                    PackageConfig config = new PackageConfig();
                    config.parseConfig(object);
                    this.platformPackageConfig.put(platform, config);
                }
                if (object1.containsKey("jlink")) {
                    JSONObject object = object1.getJSONObject("jlink");
                    JLinkConfig config = new JLinkConfig();
                    config.parseConfig(object);
                    this.platformJlinkConfig.put(platform, config);
                }
                if (object1.containsKey("jre_clip")) {
                    JSONObject object = object1.getJSONObject("jre_clip");
                    JreClipConfig config = new JreClipConfig();
                    config.parseConfig(object);
                    this.platformJreClipConfig.put(platform, config);
                }
                if (object1.containsKey("jar_clip")) {
                    JSONObject object = object1.getJSONObject("jar_clip");
                    JarClipConfig config = new JarClipConfig();
                    config.parseConfig(object);
                    this.platformJarClipConfig.put(platform, config);
                }
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
        JreClipConfig config = this.platformJreClipConfig.get(platform);
        JreClipConfig jreClipConfig = this.globalJreClipConfig.cross(config);
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
        JarClipConfig config = this.platformJarClipConfig.get(platform);
        JarClipConfig jarClipConfig = this.globalJarClipConfig.cross(config);
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
        JLinkConfig config = this.platformJlinkConfig.get(platform);
        JLinkConfig jLinkConfig = this.globalJLinkConfig.cross(config);
        jLinkConfig.setOutput(this.getJrePath(platform));
        return jLinkConfig;
    }

    /**
     * 获取交叉的打包配置
     *
     * @param platform 平台
     * @return 打包配置
     */
    public PackageConfig getCrossPackageConfig(String platform) {
        PackageConfig config = this.platformPackageConfig.get(platform);
        PackageConfig packageConfig = this.globalPackageConfig.cross(config);
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
}
