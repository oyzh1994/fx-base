package cn.oyzh.fx.pkg.jdeps;


import java.util.Set;

/**
 * jar配置
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class JDepsConfig {

    /**
     * 简要模式
     */
    private boolean summary = true;

    /**
     * 详细模式
     */
    private boolean verbose;

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

    public boolean isSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    public boolean isVerbose() {
        return verbose;
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
}
