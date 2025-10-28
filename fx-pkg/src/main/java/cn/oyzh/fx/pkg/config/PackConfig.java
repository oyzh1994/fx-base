package cn.oyzh.fx.pkg.config;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.comporess.CompressConfig;
import cn.oyzh.fx.pkg.jar.JarConfig;
import cn.oyzh.fx.pkg.jdeps.JDepsConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.jpackage.JPackageConfig;
import cn.oyzh.fx.pkg.jre.JreConfig;
import com.badlogicgames.packr.PackrConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 打包配置
 *
 * @author oyzh
 * @since 2024/6/14
 */
public class PackConfig {

    /**
     * 目标目录
     */
    private String dest;

    /**
     * jlink后的jre目录
     */
    private String jlinkJre;

    /**
     * 最小化后的jre目录
     */
    private String minimizeJre;

    /**
     * jpackage输入目录
     */
    private String jpackageInput;

    /**
     * jar解压目录
     */
    private String jarUnDir;

    /**
     * 打包方式
     * jpackage win打包的exe不能重启，mac打包的不能启动，不建议使用
     * packr
     */
    private String packMode = "jpackage";

    /**
     * 最小化后的主程序
     */
    private String minimizeManJar;

    /**
     * 主程序
     */
    private String mainJar;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用图标
     */
    private String appIcon;

    /**
     * 应用版本
     */
    private String appVersion;

    /**
     * 构建类型
     */
    private String buildType;

    /**
     * 最终压缩文件
     * 可能是以下类型
     * zip
     * tar.gz
     * AppImage
     */
    private File compressFile;

    /**
     * 打包用的jre路径
     */
    private String jrePath;

    /**
     * appImageRuntime目录
     */
    private String appImageRuntime;

    /**
     * 执行用的jdk路径
     */
    private String jdkPath;

    /**
     * 平台
     */
    private String platform;

    /**
     * jar配置
     */
    private JarConfig jarConfig;

    /**
     * jre配置
     */
    private JreConfig jreConfig;

    /**
     * jdeps配置
     */
    private JDepsConfig jDepsConfig;

    /**
     * jlink配置
     */
    private JLinkConfig jLinkConfig;

    /**
     * packr配置
     */
    private PackrConfig packrConfig;

    /**
     * jpackage配置
     */
    private JPackageConfig jPackageConfig;

    /**
     * 压缩配置
     */
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
        if (mainJar.contains("\\")) {
            return mainJar.substring(mainJar.lastIndexOf("\\") + 1);
        }
        if (mainJar.contains("/")) {
            return mainJar.substring(mainJar.lastIndexOf("/") + 1);
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

    public String mainAppVersion() {
        String appVersion = this.appVersion();
        if (StringUtil.checkCountOccurrences(appVersion, '.', 3)) {
            return appVersion.substring(0, appVersion.lastIndexOf("."));
        }
        return appVersion;
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
        return StringUtil.containsAnyIgnoreCase(this.platform, "macos");
    }

    public boolean isPlatformWindows() {
        return StringUtil.containsAnyIgnoreCase(this.platform, "win");
    }

    public boolean isPlatformLinux() {
        return StringUtil.containsAnyIgnoreCase(this.platform, "linux");
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getJlinkJre() {
        return jlinkJre;
    }

    public void setJlinkJre(String jlinkJre) {
        this.jlinkJre = jlinkJre;
    }

    public String getMinimizeJre() {
        return minimizeJre;
    }

    public void setMinimizeJre(String minimizeJre) {
        this.minimizeJre = minimizeJre;
    }

    public String getJpackageInput() {
        return jpackageInput;
    }

    public void setJpackageInput(String jpackageInput) {
        this.jpackageInput = jpackageInput;
    }

    public String getJarUnDir() {
        return jarUnDir;
    }

    public void setJarUnDir(String jarUnDir) {
        this.jarUnDir = jarUnDir;
    }

    public String getPackMode() {
        return packMode;
    }

    public void setPackMode(String packMode) {
        this.packMode = packMode;
    }

    public String getMinimizeManJar() {
        return minimizeManJar;
    }

    public void setMinimizeManJar(String minimizeManJar) {
        this.minimizeManJar = minimizeManJar;
    }

    public String getMainJar() {
        return mainJar;
    }

    public void setMainJar(String mainJar) {
        this.mainJar = mainJar;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public File getCompressFile() {
        return compressFile;
    }

    public void setCompressFile(File compressFile) {
        this.compressFile = compressFile;
    }

    public String getJrePath() {
        return jrePath;
    }

    public void setJrePath(String jrePath) {
        this.jrePath = jrePath;
    }

    public String getJdkPath() {
        return jdkPath;
    }

    public void setJdkPath(String jdkPath) {
        this.jdkPath = jdkPath;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public JarConfig getJarConfig() {
        return jarConfig;
    }

    public void setJarConfig(JarConfig jarConfig) {
        this.jarConfig = jarConfig;
    }

    public JreConfig getJreConfig() {
        return jreConfig;
    }

    public void setJreConfig(JreConfig jreConfig) {
        this.jreConfig = jreConfig;
    }

    public JDepsConfig getjDepsConfig() {
        return jDepsConfig;
    }

    public void setjDepsConfig(JDepsConfig jDepsConfig) {
        this.jDepsConfig = jDepsConfig;
    }

    public JLinkConfig getjLinkConfig() {
        return jLinkConfig;
    }

    public void setjLinkConfig(JLinkConfig jLinkConfig) {
        this.jLinkConfig = jLinkConfig;
    }

    public PackrConfig getPackrConfig() {
        return packrConfig;
    }

    public void setPackrConfig(PackrConfig packrConfig) {
        this.packrConfig = packrConfig;
    }

    public JPackageConfig getjPackageConfig() {
        return jPackageConfig;
    }

    public void setjPackageConfig(JPackageConfig jPackageConfig) {
        this.jPackageConfig = jPackageConfig;
    }

    public CompressConfig getCompressConfig() {
        return compressConfig;
    }

    public void setCompressConfig(CompressConfig compressConfig) {
        this.compressConfig = compressConfig;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getAppImageRuntime() {
        return appImageRuntime;
    }

    public void setAppImageRuntime(String appImageRuntime) {
        this.appImageRuntime = appImageRuntime;
    }
}
