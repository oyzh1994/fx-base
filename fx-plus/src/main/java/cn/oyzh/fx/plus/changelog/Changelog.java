package cn.oyzh.fx.plus.changelog;


import java.util.List;

/**
 * 更新日志
 *
 * @author oyzh
 * @since 2024/4/7
 */
public class Changelog {

    /**
     * 日期
     */
    private String date;

    /**
     * 版本
     */
    private String version;

    /**
     * 问题处理
     */
    private List<String> bugfix;

    /**
     * 优化
     */
    private List<String> optimize;

    /**
     * 新功能
     */
    private List<String> features;

    /**
     * 获取日期。
     *
     * @return 日期
     */
    public String getDate() {
        return date;
    }

    /**
     * 设置日期。
     *
     * @param date 日期
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 获取版本。
     *
     * @return 版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置版本。
     *
     * @param version 版本
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取修复。
     *
     * @return 修复
     */
    public List<String> getBugfix() {
        return bugfix;
    }

    /**
     * 设置修复。
     *
     * @param bugfix 修复
     */
    public void setBugfix(List<String> bugfix) {
        this.bugfix = bugfix;
    }

    /**
     * 获取优化。
     *
     * @return 优化
     */
    public List<String> getOptimize() {
        return optimize;
    }

    /**
     * 设置优化。
     *
     * @param optimize 优化
     */
    public void setOptimize(List<String> optimize) {
        this.optimize = optimize;
    }

    /**
     * 获取特性。
     *
     * @return 特性
     */
    public List<String> getFeatures() {
        return features;
    }

    /**
     * 设置特性。
     *
     * @param features 特性
     */
    public void setFeatures(List<String> features) {
        this.features = features;
    }
}
