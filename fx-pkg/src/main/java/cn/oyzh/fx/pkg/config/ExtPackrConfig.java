package cn.oyzh.fx.pkg.config;

import cn.oyzh.fx.pkg.packr.PackrConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/6/14
 */
@Getter
public class ExtPackrConfig extends PackrConfig {

    @Setter
    private String jlinkJre;

    @Setter
    private String minimizeJre;

    /**
     * jar解压目录
     */
    @Setter
    @Getter
    private String jarUnDir;

    /**
     *
     */
    @Setter
    @Getter
    private String minimizeManJar;

    /**
     * 主程序
     */
    @Setter
    private String mainJar;

    /**
     * 版本
     */
    @Setter
    private String version;

    /**
     * 构建类型
     */
    @Setter
    private String buildType;

    /**
     * 压缩类型
     */
    @Getter
    @Setter
    private String compressType;

    /**
     * 压缩文件名称
     */
    @Getter
    @Setter
    private String compressName;

    /**
     * 最终压缩文件
     */
    @Getter
    @Setter
    private File compressFile;

    private final Map<String, Object> properties = new HashMap<>();

    public void putProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }
}
