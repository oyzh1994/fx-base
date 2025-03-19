package cn.oyzh.fx.pkg.jlink;

import cn.oyzh.common.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * jlink配置
 *
 * @author oyzh
 * @since 2024/06/17
 */
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

    public void margeAddModules(Collection<String> addModules) {
        if (CollectionUtil.isNotEmpty(addModules)) {
            if (this.addModules == null || this.addModules.isEmpty()) {
                this.addModules = new ArrayList<>(addModules);
            } else {
                for (String addModule : addModules) {
                    if (!this.addModules.contains(addModule)) {
                        this.addModules.add(addModule);
                    }
                }
            }
        }
    }

    public String getVm() {
        return vm;
    }

    public void setVm(String vm) {
        this.vm = vm;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Integer getCompress() {
        return compress;
    }

    public void setCompress(Integer compress) {
        this.compress = compress;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isNoManPages() {
        return noManPages;
    }

    public void setNoManPages(boolean noManPages) {
        this.noManPages = noManPages;
    }

    public boolean isNoHeaderFiles() {
        return noHeaderFiles;
    }

    public void setNoHeaderFiles(boolean noHeaderFiles) {
        this.noHeaderFiles = noHeaderFiles;
    }

    public boolean isStripDebug() {
        return stripDebug;
    }

    public void setStripDebug(boolean stripDebug) {
        this.stripDebug = stripDebug;
    }

    public boolean isStripJavaDebugAttributes() {
        return stripJavaDebugAttributes;
    }

    public void setStripJavaDebugAttributes(boolean stripJavaDebugAttributes) {
        this.stripJavaDebugAttributes = stripJavaDebugAttributes;
    }

    public List<String> getAddModules() {
        return addModules;
    }

    public void setAddModules(List<String> addModules) {
        this.addModules = addModules;
    }

    public List<String> getExcludeFiles() {
        return excludeFiles;
    }

    public void setExcludeFiles(List<String> excludeFiles) {
        this.excludeFiles = excludeFiles;
    }
}
