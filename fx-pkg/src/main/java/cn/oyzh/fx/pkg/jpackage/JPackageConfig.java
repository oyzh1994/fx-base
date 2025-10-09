package cn.oyzh.fx.pkg.jpackage;


import java.io.File;
import java.util.List;

/**
 * JPackage配置
 *
 * @author oyzh
 * @since 2023/3/8
 */
public class JPackageConfig {

    /**
     * 程序名
     */
    private String name;

    /**
     * 打包类型
     */
    private String type = "app-image";

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
     * 详细信息
     */
    private boolean verbose = true;

    /**
     * vm参数
     */
    private List<String> javaOptions;

    /**
     * 是否创建开始菜单、仅windows
     */
    private boolean winMenu;

    /**
     * 是否创建桌面图标、仅windows
     */
    private boolean winShortcut;

    /**
     * 是否可选安装目录、仅windows
     */
    private boolean winDirChooser;

    /**
     * mac程序唯一id、仅macos
     */
    private String macPackageIdentifier;

    /**
     * 是否启用
     */
    private boolean enable = true;

    public String destParent() {
        return new File(dest).getParent();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getMainJar() {
        return mainJar;
    }

    public void setMainJar(String mainJar) {
        this.mainJar = mainJar;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuntimeImage() {
        return runtimeImage;
    }

    public void setRuntimeImage(String runtimeImage) {
        this.runtimeImage = runtimeImage;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isWinMenu() {
        return winMenu;
    }

    public void setWinMenu(boolean winMenu) {
        this.winMenu = winMenu;
    }

    public boolean isWinShortcut() {
        return winShortcut;
    }

    public void setWinShortcut(boolean winShortcut) {
        this.winShortcut = winShortcut;
    }

    public boolean isWinDirChooser() {
        return winDirChooser;
    }

    public void setWinDirChooser(boolean winDirChooser) {
        this.winDirChooser = winDirChooser;
    }

    public String getMacPackageIdentifier() {
        return macPackageIdentifier;
    }

    public void setMacPackageIdentifier(String macPackageIdentifier) {
        this.macPackageIdentifier = macPackageIdentifier;
    }

    public List<String> getJavaOptions() {
        return javaOptions;
    }

    public void setJavaOptions(List<String> javaOptions) {
        this.javaOptions = javaOptions;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
