package cn.oyzh.fx.pkg.jpackage;

import lombok.Data;

import java.io.File;

/**
 * JPackage配置
 *
 * @author oyzh
 * @since 2023/3/8
 */
@Data
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
     * 过程信息
     */
    private boolean verbose = true;

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

    public String destParent() {
        return new File(dest).getParent();
    }
}
