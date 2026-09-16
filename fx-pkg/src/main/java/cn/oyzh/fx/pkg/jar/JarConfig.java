package cn.oyzh.fx.pkg.jar;


import cn.oyzh.fx.pkg.ConfigMargeAble;

import java.util.Set;

/**
 * jar配置
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class JarConfig implements ConfigMargeAble<JarConfig> {

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 是否移除空jar
     */
    private Boolean removeEmpty;

    /**
     * javafx优化
     */
    private Boolean javafxOptimize;

    /**
     * 跳过的jar
     */
    private Set<String> skipsJar;

    /**
     * 排除的文件
     */
    private Set<String> excludes;

    private String javafxPath;

    public boolean isRemoveEmpty() {
        return removeEmpty == null || this.removeEmpty;
    }

    public void setRemoveEmpty(boolean removeEmpty) {
        this.removeEmpty = removeEmpty;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    public Set<String> getSkipsJar() {
        return skipsJar;
    }

    public void setSkipsJar(Set<String> skipsJar) {
        this.skipsJar = skipsJar;
    }

    public boolean isEnable() {
        return enable == null || this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isJavafxOptimize(){
        return javafxOptimize != null && this.javafxOptimize;
    }

    public Boolean getJavafxOptimize() {
        return javafxOptimize;
    }

    public void setJavafxOptimize(Boolean javafxOptimize) {
        this.javafxOptimize = javafxOptimize;
    }

    public String getJavafxPath() {
        return javafxPath;
    }

    public void setJavafxPath(String javafxPath) {
        this.javafxPath = javafxPath;
    }

    @Override
    public void marge(JarConfig config) {
        if (config == null) {
            return;
        }
        if (this.skipsJar == null) {
            this.skipsJar = config.skipsJar;
        } else if (config.skipsJar != null) {
            this.skipsJar.addAll(config.skipsJar);
        }
        if (this.excludes == null) {
            this.excludes = config.excludes;
        } else if (config.excludes != null) {
            this.excludes.addAll(config.excludes);
        }
        if (config.enable != null) {
            this.enable = config.enable;
        }
        if (config.removeEmpty != null) {
            this.removeEmpty = config.removeEmpty;
        }
        if (config.javafxOptimize != null) {
            this.javafxOptimize = config.javafxOptimize;
        }
        if (config.javafxPath != null) {
            this.javafxPath = config.javafxPath;
        }
    }
}
