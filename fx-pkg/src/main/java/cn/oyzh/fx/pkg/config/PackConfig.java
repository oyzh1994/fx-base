package cn.oyzh.fx.pkg.config;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.pkg.comporess.CompressConfig;
import cn.oyzh.fx.pkg.jar.JarConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.jpackage.JPackageConfig;
import cn.oyzh.fx.pkg.jre.JreConfig;
import cn.oyzh.fx.pkg.packr.Packr;
import cn.oyzh.fx.pkg.packr.PackrConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 打包配置
 *
 * @author oyzh
 * @since 2024/6/14
 */
@Getter
public class PackConfig {

    /**
     * 目标目录
     */
    @Setter
    private String dest;

    /**
     * jlink后的jre目录
     */
    @Setter
    private String jlinkJre;

    /**
     * 最小化后的jre目录
     */
    @Setter
    private String minimizeJre;

    /**
     * jpackage输入目录
     */
    @Setter
    private String jpackageInput;

    /**
     * jar解压目录
     */
    @Setter
    private String jarUnDir;

    /**
     * 打包方式
     * jpackage
     * packr
     */
    @Setter
    private String packMode = "packr";

    /**
     * 最小化后的主程序
     */
    @Setter
    private String minimizeManJar;

    /**
     * 主程序
     */
    @Setter
    private String mainJar;

    /**
     * 应用名称
     */
    @Setter
    private String appName;

    /**
     * 应用图标
     */
    @Setter
    private String appIcon;

    /**
     * 应用版本
     */
    @Setter
    private String appVersion;

    /**
     * 构建类型
     */
    @Setter
    private String buildType;

    /**
     * 最终压缩文件
     */
    @Setter
    private File compressFile;

    /**
     * 打包用的jre路径
     */
    @Setter
    private String jrePath;

    /**
     * 执行用的jdk路径
     */
    @Setter
    private String jdkPath;

    /**
     * 平台
     */
    @Setter
    private String platform;

    /**
     * jar配置
     */
    @Setter
    private JarConfig jarConfig;

    /**
     * jre配置
     */
    @Setter
    private JreConfig jreConfig;

    /**
     * jlink配置
     */
    @Setter
    private JLinkConfig jLinkConfig;

    /**
     * packr配置
     */
    @Setter
    private PackrConfig packrConfig;

    /**
     * jpackage配置
     */
    @Setter
    private JPackageConfig jPackageConfig;

    /**
     * 压缩配置
     */
    @Setter
    private CompressConfig compressConfig;

    /**
     * 属性
     */
    private final Map<String, Object> properties = new HashMap<>();

    public void putProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    public String mainJar() {
        if (this.minimizeManJar != null) {
            return this.minimizeManJar;
        }
        return this.mainJar;
    }

    public String mainJarName() {
        String mainJar = this.mainJar();
        if (mainJar.contains("/")) {
            return mainJar.substring(mainJar.lastIndexOf("/") + 1);
        }
        if (mainJar.contains("\\")) {
            return mainJar.substring(mainJar.lastIndexOf("\\") + 1);
        }
        return mainJar;
    }

    public String outPath() {
        if (this.compressFile != null) {
            return this.compressFile.getPath();
        }
        return this.dest;
    }

    public String appVersion() {
        if (this.appVersion == null) {
            return null;
        }
        if (this.appVersion.toLowerCase().startsWith("v")) {
            return this.appVersion.substring(1);
        }
        return this.appVersion;
    }

    public boolean isParkByPackr() {
        return this.packMode.equalsIgnoreCase("packr");
    }

    public String jrePath() {
        if (this.minimizeJre != null) {
            return this.minimizeJre;
        }
        if (this.jlinkJre != null) {
            return this.jlinkJre;
        }
        return this.jrePath;
    }

    public boolean isPlatformMacos() {
        return StrUtil.containsAnyIgnoreCase(this.platform, "macos");
    }

    public boolean isPlatformWindows() {
        return StrUtil.containsAnyIgnoreCase(this.platform, "win");
    }
}
