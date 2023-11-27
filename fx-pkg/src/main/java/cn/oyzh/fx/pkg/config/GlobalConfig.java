package cn.oyzh.fx.pkg.config;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.oyzh.fx.pkg.clip.clipper.JarClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JreClipConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.packager.PackageConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全局配置
 *
 * @author oyzh
 * @since 2023/11/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GlobalConfig extends BaseConfig {

    /**
     * jar路径
     */
    private String jarPath;

    /**
     * jdk路径
     */
    private String jdkPath;

    /**
     * 目标路径
     */
    private String destPath;

    /**
     * 是否保留过程文件
     */
    private boolean retainDuringDir;

    /**
     * jlink配置
     */
    private JLinkConfig jLinkConfig = new JLinkConfig();

    /**
     * jar裁剪配置
     */
    private JarClipConfig jarClipConfig = new JarClipConfig();

    /**
     * jre裁剪配置
     */
    private JreClipConfig jreClipConfig = new JreClipConfig();

    /**
     * 打包配置
     */
    private PackageConfig packageConfig = new PackageConfig();

    @Override
    public void parseConfig(JSONObject object1) {
        String jarPath = object1.getStr("jarPath");
        if (jarPath != null) {
            this.jarPath = jarPath;
        }
        String destPath = object1.getStr("destPath");
        if (destPath != null) {
            this.destPath = destPath;
        }
        String jdkPath = object1.getStr("jdkPath");
        if (jdkPath != null) {
            this.jdkPath = jdkPath;
        }
        Boolean retainDuringDir = object1.getBool("retainDuringDir");
        if (retainDuringDir != null) {
            this.retainDuringDir = retainDuringDir;
        }
        if (object1.containsKey("package")) {
            JSONObject object = object1.getJSONObject("package");
            this.packageConfig.parseConfig(object);
        }
        if (object1.containsKey("jlink")) {
            JSONObject object = object1.getJSONObject("jlink");
            this.jLinkConfig.parseConfig(object);
        }
        if (object1.containsKey("jre_clip")) {
            JSONObject object = object1.getJSONObject("jre_clip");
            this.jreClipConfig.parseConfig(object);
        }
        if (object1.containsKey("jar_clip")) {
            JSONObject object = object1.getJSONObject("jar_clip");
            this.jarClipConfig.parseConfig(object);
        }
    }

    /**
     * 加载配置
     *
     * @param configPath 配置路径
     * @return GlobalConfig
     */
    public static GlobalConfig loadConfig(String configPath) {
        String text = ResourceUtil.readUtf8Str(configPath);
        JSONObject object = JSONUtil.parseObj(text);
        GlobalConfig config = new GlobalConfig();
        config.parseConfig(object);
        return config;
    }
}
