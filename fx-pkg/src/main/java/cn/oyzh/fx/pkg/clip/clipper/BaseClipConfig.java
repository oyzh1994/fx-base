package cn.oyzh.fx.pkg.clip.clipper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * 基础裁剪配置
 *
 * @author oyzh
 * @since 2022/12/14
 */
@Data
public class BaseClipConfig {

    /**
     * 源目录
     */
    private String src;

    /**
     * 目标目录
     */
    private String dest;

    /**
     * 排除的文件
     */
    private Set<String> excludeFiles = new HashSet<>();

    /**
     * 解析配置
     *
     * @param configPath 配置路径
     */
    public void parseConfig(@NonNull String configPath) {
        String text = FileUtil.readUtf8String(configPath);
        JSONObject object = JSONObject.parseObject(text);
        this.fromConfig(object);
    }

    /**
     * 解析配置
     *
     * @param object json对象
     */
    protected void fromConfig(JSONObject object) {
        this.src = object.getString("src");
        this.dest = object.getString("dest");
        JSONArray excludeFiles = object.getJSONArray("excludeFiles");
        if (CollUtil.isNotEmpty(excludeFiles)) {
            for (Object excludeFile : excludeFiles) {
                this.excludeFiles.add((String) excludeFile);
            }
        }
    }

    /**
     * 应用默认配置
     */
    public void applyDefault() {
        // 排除常见类型
        this.excludeFiles.add(".md");
        this.excludeFiles.add(".idx");
        this.excludeFiles.add(".xsd");
        this.excludeFiles.add(".tooling");
        this.excludeFiles.add(".schemas");
        this.excludeFiles.add(".handlers");
        this.excludeFiles.add(".processors");
        this.excludeFiles.add(".kotlin_module");

        // 排除常见文件
        this.excludeFiles.add("pom.xml");
        this.excludeFiles.add("pom.properties");
        this.excludeFiles.add("DEPENDENCIES");
        this.excludeFiles.add("NOTICE");
        this.excludeFiles.add("LICENSE");
        this.excludeFiles.add("README.md");
        this.excludeFiles.add("README.txt");
        this.excludeFiles.add("changelog.txt");
        this.excludeFiles.add("release-timestamp.txt");
        this.excludeFiles.add("latestchanges.html");
        this.excludeFiles.add("AUTHORS");
        this.excludeFiles.add("ASSEMBLY_EXCEPTION");
        this.excludeFiles.add("ADDITIONAL_LICENSE_INFO");

        // 排除无效文件
//        this.excludeFiles.add("MANIFEST.MF");
        this.excludeFiles.add("spring.factories");
        this.excludeFiles.add("reactor.blockhound.integration.BlockHoundIntegration");

        // graalvm相关
        this.excludeFiles.add("aot.factories");
        this.excludeFiles.add("reflectionconfig.json");
    }

    /**
     * 从配置文件生成配置对象
     *
     * @param configPath 配置文件路径
     * @return 配置对象
     */
    public static BaseClipConfig fromConfig(String configPath) {
        BaseClipConfig config = new BaseClipConfig();
        config.parseConfig(configPath);
        config.applyDefault();
        return config;
    }

}
