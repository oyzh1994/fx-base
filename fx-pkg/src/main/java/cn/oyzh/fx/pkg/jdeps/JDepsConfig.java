package cn.oyzh.fx.pkg.jdeps;


import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.fx.pkg.ConfigMargeAble;

import java.util.Set;

/**
 * jar配置
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class JDepsConfig implements ConfigMargeAble<JDepsConfig> {

    /**
     * 汇总信息
     */
    private Boolean summary;

    /**
     * 详细模式
     */
    private Boolean verbose;

    /**
     * 跳过的文件
     */
    private Set<String> skips;

    /**
     * 排除的文件
     */
    private Set<String> excludes;

    /**
     * 多版本
     */
    private Integer multiRelease;

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

    public boolean isSummary() {
        return BooleanUtil.isTrue(this.summary);
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    public boolean isVerbose() {
        return BooleanUtil.isTrue(this.verbose);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Set<String> getSkips() {
        return skips;
    }

    public void setSkips(Set<String> skips) {
        this.skips = skips;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    public Integer getMultiRelease() {
        return multiRelease;
    }

    public void setMultiRelease(Integer multiRelease) {
        this.multiRelease = multiRelease;
    }

    @Override
    public void marge(JDepsConfig config) {
        if (config == null) {
            return;
        }
        if (config.multiRelease != null) {
            this.multiRelease = config.multiRelease;
        }
        if (this.skips == null) {
            this.skips = config.skips;
        } else if (config.skips != null) {
            this.skips.addAll(config.skips);
        }
        if (this.excludes == null) {
            this.excludes = config.excludes;
        } else if (config.excludes != null) {
            this.excludes.addAll(config.excludes);
        }
        if (config.enable != null) {
            this.enable = config.enable;
        }
        if (config.summary != null) {
            this.summary = config.summary;
        }
        if (config.verbose != null) {
            this.verbose = config.verbose;
        }
    }
}
