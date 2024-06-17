package cn.oyzh.fx.pkg.jar;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.packr.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author oyzh
 * @since 2024/6/17
 */
public class JarConfigParser implements ConfigParser<JarConfig> {

    @Override
    public JarConfig parse(String configFile) {
        JSONObject object = JSONUtil.parseObj(FileUtil.readUtf8String(configFile));
        JarConfig config = new JarConfig();
        JSONArray excludes = object.getJSONArray("excludes");
        if (excludes != null) {
            config.setExcludes(new HashSet<>());
            for (Object o : excludes) {
                config.getExcludes().add(o.toString());
            }
        }
        config.setRemoveEmpty(object.getBool("removeEmpty", true));
        return config;
    }

    public static JarConfig parseConfig(String configFile) {
        return new JarConfigParser().parse(configFile);
    }
}
