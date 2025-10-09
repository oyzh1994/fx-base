package cn.oyzh.fx.pkg.jlink;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

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
        JSONArray excludeFiles = object.getJSONArray("exclude-files");
        if (excludeFiles != null) {
            config.setExcludeFiles(new ArrayList<>());
            for (Object o : excludeFiles) {
                config.getExcludeFiles().add(o.toString());
            }
        }
        JSONArray addModules = object.getJSONArray("add-modules");
        if (addModules != null) {
            config.setAddModules(new ArrayList<>());
            for (Object o : addModules) {
                config.getAddModules().add(o.toString());
            }
        }
        // 临时jre目录
        String tmpJreDir = new File(FileUtil.getTmpDir(), "_temp_jre_" + UUID.fastUUID().toString(true)).getPath();
        config.setVm(StringUtil.emptyToDefault(object.getString("vm"), "server"));
        config.setOutput(StringUtil.emptyToDefault(object.getString("output"), tmpJreDir));
        config.setCompress(object.getIntValue("compress", 2));
        config.setVerbose(object.getBooleanValue("verbose", true));
        config.setNoManPages(object.getBooleanValue("no-man-pages", true));
        config.setStripDebug(object.getBooleanValue("strip-debug", true));
        config.setNoHeaderFiles(object.getBooleanValue("no-header-files", true));
        config.setStripJavaDebugAttributes(object.getBooleanValue("strip-java-debug-attributes", true));
        return config;
    }

    public static JLinkConfig parseConfig(JSONObject object) {
        return new JLinkConfigParser().parse(object);
    }

    public static JLinkConfig parseConfig(String configFile) {
        return new JLinkConfigParser().parse(configFile);
    }
}
