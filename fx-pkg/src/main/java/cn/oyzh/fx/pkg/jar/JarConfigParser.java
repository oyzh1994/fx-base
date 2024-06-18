package cn.oyzh.fx.pkg.jar;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.oyzh.fx.pkg.ConfigParser;

import java.util.HashSet;

/**
 * jar配置解析器
 *
 * @author oyzh
 * @since 2024/6/17
 */
public class JarConfigParser implements ConfigParser<JarConfig> {

    @Override
    public JarConfig parse(JSONObject object) {
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

    public static JarConfig parseConfig(JSONObject object) {
        return new JarConfigParser().parse(object);
    }

    public static JarConfig parseConfig(String configFile) {
        return new JarConfigParser().parse(configFile);
    }
}
