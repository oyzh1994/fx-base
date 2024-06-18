package cn.oyzh.fx.pkg.jlink;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.oyzh.fx.pkg.ConfigParser;

import java.io.File;
import java.util.ArrayList;

/**
 * jlink配置解析器
 *
 * @author oyzh
 * @since 2024/6/17
 */
public class JLinkConfigParser implements ConfigParser<JLinkConfig> {

    @Override
    public JLinkConfig parse(JSONObject object) {
        JLinkConfig config = new JLinkConfig();
        JSONArray excludeFiles = object.getJSONArray("excludeFiles");
        if (excludeFiles != null) {
            config.setExcludeFiles(new ArrayList<>());
            for (Object o : excludeFiles) {
                config.getExcludeFiles().add(o.toString());
            }
        }
        JSONArray addModules = object.getJSONArray("addModules");
        if (addModules != null) {
            config.setAddModules(new ArrayList<>());
            for (Object o : addModules) {
                config.getAddModules().add(o.toString());
            }
        }
        // 临时jre目录
        String tmpJreDir = new File(FileUtil.getTmpDir(), "_temp_jre_" + UUID.fastUUID().toString(true)).getPath();
        config.setVm(object.getStr("vm", "server"));
        config.setOutput(object.getStr("output", tmpJreDir));
        config.setCompress(object.getInt("compress", 2));
        config.setVerbose(object.getBool("verbose", true));
        config.setNoManPages(object.getBool("noManPages", true));
        config.setStripDebug(object.getBool("stripDebug", true));
        config.setNoHeaderFiles(object.getBool("noHeaderFiles", true));
        config.setStripJavaDebugAttributes(object.getBool("stripJavaDebugAttributes", true));
        return config;
    }

    public static JLinkConfig parseConfig(JSONObject object) {
        return new JLinkConfigParser().parse(object);
    }

    public static JLinkConfig parseConfig(String configFile) {
        return new JLinkConfigParser().parse(configFile);
    }
}
