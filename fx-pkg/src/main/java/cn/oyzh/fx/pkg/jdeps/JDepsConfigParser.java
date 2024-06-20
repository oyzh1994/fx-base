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
        return config;
    }

    public static JDepsConfig parseConfig(JSONObject object) {
        return new JDepsConfigParser().parse(object);
    }

    public static JDepsConfig parseConfig(String configFile) {
        return new JDepsConfigParser().parse(configFile);
    }
}
