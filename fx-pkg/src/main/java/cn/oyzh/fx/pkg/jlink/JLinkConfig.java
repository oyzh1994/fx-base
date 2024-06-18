package cn.oyzh.fx.pkg.jlink;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * jlink配置
 *
 * @author oyzh
 * @since 2023/3/8
 */
@Data
public class JLinkConfig {

    /**
     * vm类型
     */
    private String vm = "server";

    /**
     * 输出目录
     */
    private String output;

    /**
     * 压缩等级
     */
    private Integer compress = 2;

    /**
     * 打印过程日志
     */
    private boolean verbose = true;

    /**
     * 不无需主页面
     */
    private boolean noManPages = true;

    /**
     * 无需头文件
     */
    private boolean noHeaderFiles = true;

    /**
     * 去除debug文件
     */
    private boolean stripDebug = true;

    /**
     * 去除debug属性
     */
    private boolean stripJavaDebugAttributes = true;

    /**
     * 添加的模块
     */
    private List<String> addModules;

    /**
     * 排除文件
     */
    private List<String> excludeFiles;

    public void parseConfig(JSONObject object) {

    }

    // @Override
    // public JLinkConfig clone() {
    //     JLinkConfig config = new JLinkConfig();
    //     config.vm = this.vm;
    //     config.output = this.output;
    //     config.enable = this.enable;
    //     config.verbose = this.verbose;
    //     config.compress = this.compress;
    //     config.addModules = this.addModules;
    //     config.stripDebug = this.stripDebug;
    //     config.noManPages = this.noManPages;
    //     config.excludeFiles = this.excludeFiles;
    //     config.noHeaderFiles = this.noHeaderFiles;
    //     config.stripJavaDebugAttributes = this.stripJavaDebugAttributes;
    //     return config;
    // }
    //
    // @Override
    // public JLinkConfig cross(Object o) {
    //     JLinkConfig config1 = this.clone();
    //     if (o instanceof JLinkConfig config) {
    //         config1.enable = config.enable;
    //         config1.verbose = config.verbose;
    //         config1.stripDebug = config.stripDebug;
    //         config1.noManPages = config.noManPages;
    //         config1.noHeaderFiles = config.noHeaderFiles;
    //         config1.stripJavaDebugAttributes = config.stripJavaDebugAttributes;
    //         if (config.vm != null) {
    //             config1.vm = config.vm;
    //         }
    //         if (config.addModules != null) {
    //             if (config1.addModules == null) {
    //                 config1.addModules = config.addModules;
    //             } else {
    //                 config1.addModules.addAll(config.addModules);
    //             }
    //         }
    //         if (config.excludeFiles != null) {
    //             if (config1.excludeFiles == null) {
    //                 config1.excludeFiles = config.excludeFiles;
    //             } else {
    //                 config1.excludeFiles.addAll(config.excludeFiles);
    //             }
    //         }
    //         if (config.compress != null) {
    //             config1.compress = config.compress;
    //         }
    //         if (config.output != null) {
    //             config1.output = config.output;
    //         }
    //     }
    //     return config1;
    // }
}
