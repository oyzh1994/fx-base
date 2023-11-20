package cn.oyzh.fx.pkg.clip.clipper;

import cn.oyzh.fx.pkg.config.BaseConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * 基础裁剪配置
 *
 * @author oyzh
 * @since 2022/12/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseClipConfig extends BaseConfig {

    /**
     * 源目录
     */
    protected String src;

    /**
     * 目标目录
     */
    protected String dest;

    /**
     * 排除的文件
     */
    protected Set<String> excludeFiles;

    @Override
    public void parseConfig(JSONObject object) {
        super.parseConfig(object);
        JSONArray excludeFiles = object.getJSONArray("excludeFiles");
        if (excludeFiles != null) {
            this.excludeFiles = new HashSet<>();
            for (Object o : excludeFiles) {
                this.excludeFiles.add(o.toString());
            }
        }
        String src = object.getString("src");
        if (src != null) {
            this.src = src;
        }
        String dest = object.getString("dest");
        if (dest != null) {
            this.dest = dest;
        }
    }

//     /**
//      * 应用默认配置
//      */
//     public void applyDefault() {
//         // 排除常见类型
//         this.excludeFiles.add(".md");
//         this.excludeFiles.add(".idx");
//         this.excludeFiles.add(".xsd");
//         this.excludeFiles.add(".tooling");
//         this.excludeFiles.add(".schemas");
//         this.excludeFiles.add(".handlers");
//         this.excludeFiles.add(".processors");
//         this.excludeFiles.add(".kotlin_module");
//
//         // 排除常见文件
//         this.excludeFiles.add("pom.xml");
//         this.excludeFiles.add("pom.properties");
//         this.excludeFiles.add("DEPENDENCIES");
//         this.excludeFiles.add("NOTICE");
//         this.excludeFiles.add("LICENSE");
//         this.excludeFiles.add("README.md");
//         this.excludeFiles.add("README.txt");
//         this.excludeFiles.add("changelog.txt");
//         this.excludeFiles.add("release-timestamp.txt");
//         this.excludeFiles.add("latestchanges.html");
//         this.excludeFiles.add("AUTHORS");
//         this.excludeFiles.add("ASSEMBLY_EXCEPTION");
//         this.excludeFiles.add("ADDITIONAL_LICENSE_INFO");
//
//         // 排除无效文件
// //        this.excludeFiles.add("MANIFEST.MF");
//         this.excludeFiles.add("spring.factories");
//         this.excludeFiles.add("reactor.blockhound.integration.BlockHoundIntegration");
//
//         // graalvm相关
//         this.excludeFiles.add("aot.factories");
//         this.excludeFiles.add("reflectionconfig.json");
//     }
}
