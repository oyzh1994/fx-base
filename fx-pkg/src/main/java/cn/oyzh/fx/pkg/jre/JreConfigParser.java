package cn.oyzh.fx.pkg.jre;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.oyzh.fx.pkg.ConfigParser;

import java.util.ArrayList;

/**
 * jre配置解析器
 *
 * @author oyzh
 * @since 2024/6/17
 */
public class JreConfigParser implements ConfigParser<JreConfig> {

    @Override
    public JreConfig parse(JSONObject object) {
        JreConfig config = new JreConfig();
        JSONArray excludes = object.getJSONArray("excludes");
        if (excludes != null) {
            config.setExcludes(new ArrayList<>());
            for (Object o : excludes) {
                config.getExcludes().add(o.toString());
            }
        }
        return config;
    }

    public static JreConfig parseConfig(JSONObject object) {
        return new JreConfigParser().parse(object);
    }

    public static JreConfig parseConfig(String configFile) {
        return new JreConfigParser().parse(configFile);
    }
}
