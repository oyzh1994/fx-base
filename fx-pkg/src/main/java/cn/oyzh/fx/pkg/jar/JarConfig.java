package cn.oyzh.fx.pkg.jar;


import java.util.Set;

/**
 * jar配置
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class JarConfig {

    /**
     * 是否移除空jar
     */
    private boolean removeEmpty = true;

    /**
     * 排除的文件
     */
    private Set<String> excludes;

    public boolean isRemoveEmpty() {
        return removeEmpty;
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
}
