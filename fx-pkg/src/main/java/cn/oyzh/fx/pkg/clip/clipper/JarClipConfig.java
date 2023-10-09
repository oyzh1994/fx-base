package cn.oyzh.fx.pkg.clip.clipper;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

    /**
     * 是否保存临时目录
     */
    private boolean retainTemp;

    /**
     * 是否删除空jar
     */
    private boolean delEmptyJar;

    /**
     * 排除的jar
     */
    private Set<String> excludeJars = new HashSet<>();

    /**
     * 排除的类
     */
    private Set<String> excludeClasses = new HashSet<>();

    @Override
    protected void fromConfig(JSONObject object) {
        super.fromConfig(object);
        this.retainTemp = object.getBooleanValue("retainTemp");
        this.delEmptyJar = object.getBooleanValue("delEmptyJar");
        JSONArray excludeJars = object.getJSONArray("excludeJars");
        if (CollUtil.isNotEmpty(excludeJars)) {
            for (Object excludeJar : excludeJars) {
                this.excludeJars.add((String) excludeJar);
            }
        }
        JSONArray excludeClasses = object.getJSONArray("excludeClasses");
        if (CollUtil.isNotEmpty(excludeClasses)) {
            for (Object excludeClass : excludeClasses) {
                this.excludeClasses.add((String) excludeClass);
            }
        }
    }

    @Override
    public void applyDefault() {
        super.applyDefault();
        // 排除的jar
        this.excludeJars.add("lombok-");
        // 排除模块化信息
        this.excludeClasses.add("module-info");
        // 排除包信息
        this.excludeClasses.add("package-info");

        // windows相关
        this.getExcludeFiles().add("ucrtbase.dll");
        this.getExcludeFiles().add("msvcp140.dll");
        this.getExcludeFiles().add("msvcp140_2.dll");
        this.getExcludeFiles().add("vcruntime140.dll");
        this.getExcludeFiles().add("api-ms-win-crt-private-l1-1-0.dll");
        this.getExcludeFiles().add("vcruntime140_1.dll");
        this.getExcludeFiles().add("api-ms-win-crt-math-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-multibyte-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-string-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-stdio-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-runtime-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-environment-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-convert-l1-1-0.dll");
        this.getExcludeFiles().add("msvcp140_1.dll");
        this.getExcludeFiles().add("api-ms-win-core-file-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-time-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-localization-l1-2-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-filesystem-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-synch-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-processthreads-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-process-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-libraryloader-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-heap-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-conio-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-sysinfo-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-processenvironment-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-rtlsupport-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-processthreads-l1-1-1.dll");
        this.getExcludeFiles().add("api-ms-win-crt-utility-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-crt-locale-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-timezone-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-synch-l1-2-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-memory-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-heap-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-console-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-console-l1-2-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-debug-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-util-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-string-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-namedpipe-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-interlocked-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-handle-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-file-l2-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-file-l1-2-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-errorhandling-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-datetime-l1-1-0.dll");
        this.getExcludeFiles().add("api-ms-win-core-profile-l1-1-0.dll");
    }

    /**
     * 从配置文件生成配置对象
     *
     * @param configPath 配置文件路径
     * @return 配置对象
     */
    public static JarClipConfig fromConfig(String configPath) {
        JarClipConfig config = new JarClipConfig();
        config.parseConfig(configPath);
        config.applyDefault();
        return config;
    }
}
