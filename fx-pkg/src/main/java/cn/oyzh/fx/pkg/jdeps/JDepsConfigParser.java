package cn.oyzh.fx.pkg.jdeps;

import cn.oyzh.fx.pkg.ConfigParser;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

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
        config.setMultiRelease(object.getInteger("multi-release"));
        config.setSummary(object.getBooleanValue("summary", true));
        config.setVerbose(object.getBooleanValue("verbose", false));
        return config;
    }

    public static JDepsConfig parseConfig(JSONObject object) {
        return new JDepsConfigParser().parse(object);
    }

    public static JDepsConfig parseConfig(String configFile) {
        return new JDepsConfigParser().parse(configFile);
    }
}
