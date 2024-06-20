package cn.oyzh.fx.pkg.jdeps;

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
public class JDepsConfigParser implements ConfigParser<JDepsConfig> {

    @Override
    public JDepsConfig parse(JSONObject object) {
        JDepsConfig config = new JDepsConfig();
        JSONArray skips = object.getJSONArray("skips");
        if (skips != null) {
            config.setSkips(new HashSet<>());
            for (Object o : skips) {
                config.getSkips().add(o.toString());
            }
        }
        JSONArray excludes = object.getJSONArray("excludes");
        if (excludes != null) {
            config.setExcludes(new HashSet<>());
            for (Object o : excludes) {
                config.getExcludes().add(o.toString());
            }
        }
        config.setMultiRelease(object.getInt("multi-release"));
        config.setSummary(object.getBool("summary", true));
        config.setVerbose(object.getBool("verbose", false));
        return config;
    }

    public static JDepsConfig parseConfig(JSONObject object) {
        return new JDepsConfigParser().parse(object);
    }

    public static JDepsConfig parseConfig(String configFile) {
        return new JDepsConfigParser().parse(configFile);
    }
}
