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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getBugfix() {
        return bugfix;
    }

    public void setBugfix(List<String> bugfix) {
        this.bugfix = bugfix;
    }

    public List<String> getOptimize() {
        return optimize;
    }

    public void setOptimize(List<String> optimize) {
        this.optimize = optimize;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }
}
