package cn.oyzh.fx.pkg.clip.clipper;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * jar裁剪配置
 *
 * @author oyzh
 * @since 2022/12/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JarClipConfig extends BaseClipConfig {

    // /**
    //  * 是否保存临时目录
    //  */
    // private boolean retainTemp;

    /**
     * 是否删除空jar
     */
    private boolean delEmptyJar = true;

    /**
     * 排除的jar
     */
    private Set<String> excludeJars;

    /**
     * 排除的类
     */
    private Set<String> excludeClasses;

    // @Override
    // public void applyDefault() {
    //     super.applyDefault();
    //     // 排除的jar
    //     this.excludeJars.add("lombok-");
    //     // 排除模块化信息
    //     this.excludeClasses.add("module-info");
    //     // 排除包信息
    //     this.excludeClasses.add("package-info");
    //
    //     // windows相关
    //     // this.getExcludeFiles().add("ucrtbase.dll");
    //     // this.getExcludeFiles().add("msvcp140.dll");
    //     // this.getExcludeFiles().add("msvcp140_2.dll");
    //     // this.getExcludeFiles().add("vcruntime140.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-private-l1-1-0.dll");
    //     // this.getExcludeFiles().add("vcruntime140_1.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-math-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-multibyte-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-string-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-stdio-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-runtime-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-environment-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-convert-l1-1-0.dll");
    //     // this.getExcludeFiles().add("msvcp140_1.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-file-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-time-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-localization-l1-2-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-filesystem-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-synch-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-processthreads-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-process-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-libraryloader-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-heap-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-conio-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-sysinfo-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-processenvironment-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-rtlsupport-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-processthreads-l1-1-1.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-utility-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-crt-locale-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-timezone-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-synch-l1-2-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-memory-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-heap-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-console-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-console-l1-2-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-debug-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-util-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-string-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-namedpipe-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-interlocked-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-handle-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-file-l2-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-file-l1-2-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-errorhandling-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-datetime-l1-1-0.dll");
    //     // this.getExcludeFiles().add("api-ms-win-core-profile-l1-1-0.dll");
    // }

    @Override
    public void parseConfig(JSONObject object) {
        super.parseConfig(object);
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
        Boolean delEmptyJar = object.getBool("delEmptyJar");
        if (delEmptyJar != null) {
            this.delEmptyJar = delEmptyJar;
        }
        // Boolean retainTemp = object.getBoolean("retainTemp");
        // if (retainTemp != null) {
        //     this.retainTemp = retainTemp;
        // }
    }

    @Override
    public JarClipConfig clone() {
        JarClipConfig config = new JarClipConfig();
        config.src = this.src;
        config.dest = this.dest;
        config.enable = this.enable;
        config.delEmptyJar = this.delEmptyJar;
        config.excludeJars = this.excludeJars;
        config.excludeFiles = this.excludeFiles;
        config.excludeClasses = this.excludeClasses;
        return config;
    }

    @Override
    public JarClipConfig cross(Object o) {
        JarClipConfig config1 = this.clone();
        if (o instanceof JarClipConfig config) {
            config1.enable = config.enable;
            config1.delEmptyJar = config.delEmptyJar;
            if (config.excludeJars != null) {
                if (config1.excludeJars == null) {
                    config1.excludeJars = config.excludeJars;
                } else {
                    config1.excludeJars.addAll(config.excludeJars);
                }
            }
            if (config.excludeFiles != null) {
                if (config1.excludeFiles == null) {
                    config1.excludeFiles = config.excludeFiles;
                } else {
                    config1.excludeFiles.addAll(config.excludeFiles);
                }
            }
            if (config.excludeClasses != null) {
                if (config1.excludeClasses == null) {
                    config1.excludeClasses = config.excludeClasses;
                } else {
                    config1.excludeClasses.addAll(config.excludeClasses);
                }
            }
            if (config.src != null) {
                config1.src = config.src;
            }
            if (config.dest != null) {
                config1.dest = config.dest;
            }
        }
        return config1;
    }
}
