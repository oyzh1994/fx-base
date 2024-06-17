package cn.oyzh.fx.pkg.jar;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * jar裁剪配置
 *
 * @author oyzh
 * @since 2022/12/14
 */
@Data
public class JarConfig {

    /**
     * 是否删除空jar
     */
    private boolean delEmptyJar = true;

    /**
     * 排除的文件
     */
    private Set<String> excludes;

    /**
     * 排除的jar
     */
    private Set<String> excludeJars;

    /**
     * 排除的类
     */
    private Set<String> excludeClasses;

    /**
     * 排除的文件
     */
    protected Set<String> excludeFiles;

    public void parseConfig(JSONObject object) {
        JSONArray excludes = object.getJSONArray("excludes");
        if (excludes != null) {
            this.excludes = new HashSet<>();
            for (Object o : excludes) {
                this.excludes.add(o.toString());
            }
        }
        JSONArray excludeJars = object.getJSONArray("excludeJars");
        if (excludeJars != null) {
            this.excludeJars = new HashSet<>();
            for (Object o : excludeJars) {
                this.excludeJars.add(o.toString());
            }
        }
        JSONArray excludeClasses = object.getJSONArray("excludeClasses");
        if (excludeClasses != null) {
            this.excludeClasses = new HashSet<>();
            for (Object o : excludeClasses) {
                this.excludeClasses.add(o.toString());
            }
        }
        JSONArray excludeFiles = object.getJSONArray("excludeFiles");
        if (excludeFiles != null) {
            this.excludeFiles = new HashSet<>();
            for (Object o : excludeFiles) {
                this.excludeFiles.add(o.toString());
            }
        }
        Boolean delEmptyJar = object.getBool("delEmptyJar");
        if (delEmptyJar != null) {
            this.delEmptyJar = delEmptyJar;
        }
    }
}
