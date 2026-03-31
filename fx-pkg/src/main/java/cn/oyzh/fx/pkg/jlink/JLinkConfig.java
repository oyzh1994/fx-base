package cn.oyzh.fx.pkg.jlink;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.pkg.ConfigMargeAble;

import java.util.Collection;
import java.util.HashSet;
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
    private String vm;

    /**
     * 输出目录
     */
    private String output;

    /**
     * 压缩等级
     */
    private String compress;

    /**
     * 打印过程日志
     */
    private Boolean verbose;

    /**
     * 无需man手册
     */
    private Boolean noManPages;

    /**
     * 无需头文件
     */
    private Boolean noHeaderFiles;

    /**
     * 去除debug文件
     */
    private Boolean stripDebug;

    /**
     * 去除debug属性
     */
    private Boolean stripJavaDebugAttributes;

    /**
     * 忽略签名信息
     */
    private Boolean ignoreSigningInformation;

    /**
     * 添加的模块
     */
    private Set<String> addModules;

    /**
     * 排除文件
     */
    private Set<String> excludeFiles;

    /**
     * 是否启用
     */
    private Boolean enable;

    public boolean isEnable() {
        return BooleanUtil.isTrue(this.enable);
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

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

    public String getCompress() {
        return compress;
    }

    public void setCompress(String compress) {
        this.compress = compress;
    }

    public boolean isVerbose() {
        return BooleanUtil.isTrue(this.verbose);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isNoManPages() {
        return BooleanUtil.isTrue(this.noManPages);
    }

    public void setNoManPages(boolean noManPages) {
        this.noManPages = noManPages;
    }

    public boolean isNoHeaderFiles() {
        return BooleanUtil.isTrue(this.noHeaderFiles);
    }

    public void setNoHeaderFiles(boolean noHeaderFiles) {
        this.noHeaderFiles = noHeaderFiles;
    }

    public boolean isStripDebug() {
        return BooleanUtil.isTrue(this.stripDebug);
    }

    public void setStripDebug(boolean stripDebug) {
        this.stripDebug = stripDebug;
    }

    public boolean isStripJavaDebugAttributes() {
        return BooleanUtil.isTrue(this.stripJavaDebugAttributes);
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

    public boolean isIgnoreSigningInformation() {
        return BooleanUtil.isTrue(this.ignoreSigningInformation);
    }

    public void setIgnoreSigningInformation(boolean ignoreSigningInformation) {
        this.ignoreSigningInformation = ignoreSigningInformation;
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
        if (config.enable != null) {
            this.enable = config.enable;
        }
        if (config.verbose != null) {
            this.verbose = config.verbose;
        }
        if (config.stripDebug != null) {
            this.stripDebug = config.stripDebug;
        }
        if (config.noManPages != null) {
            this.noManPages = config.noManPages;
        }
        if (config.noHeaderFiles != null) {
            this.noHeaderFiles = config.noHeaderFiles;
        }
        if (config.ignoreSigningInformation != null) {
            this.ignoreSigningInformation = config.ignoreSigningInformation;
        }
        if (config.stripJavaDebugAttributes != null) {
            this.stripJavaDebugAttributes = config.stripJavaDebugAttributes;
        }
    }
}
