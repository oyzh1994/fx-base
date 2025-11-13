package cn.oyzh.fx.pkg.jlink;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.pkg.ConfigMargeAble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * jlink配置
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class JLinkConfig implements ConfigMargeAble<JLinkConfig> {

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
     * 无需man手册
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
    private Set<String> addModules;

    /**
     * 排除文件
     */
    private Set<String> excludeFiles;

    public void margeAddModules(Collection<String> addModules) {
        if (CollectionUtil.isNotEmpty(addModules)) {
            if (this.addModules == null || this.addModules.isEmpty()) {
                this.addModules = new HashSet<>(addModules);
            } else {
                this.addModules.addAll(addModules);
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

    public Set<String> getAddModules() {
        return addModules;
    }

    public void setAddModules(Set<String> addModules) {
        this.addModules = addModules;
    }

    public Set<String> getExcludeFiles() {
        return excludeFiles;
    }

    public void setExcludeFiles(Set<String> excludeFiles) {
        this.excludeFiles = excludeFiles;
    }

    @Override
    public void marge(JLinkConfig config) {
        if (config == null) {
            return;
        }
        if (config.vm != null) {
            this.vm = config.vm;
        }
        if (config.output != null) {
            this.output = config.output;
        }
        if (config.compress != null) {
            this.compress = config.compress;
        }
        if (this.addModules == null) {
            this.addModules = config.addModules;
        } else if (config.addModules != null) {
            this.addModules.addAll(config.addModules);
        }
        if (this.excludeFiles == null) {
            this.excludeFiles = config.excludeFiles;
        } else if (config.excludeFiles != null) {
            this.excludeFiles.addAll(config.excludeFiles);
        }
        this.verbose = config.verbose;
        this.stripDebug = config.stripDebug;
        this.noManPages = config.noManPages;
        this.noHeaderFiles = config.noHeaderFiles;
        this.stripJavaDebugAttributes = config.stripJavaDebugAttributes;
    }
}
