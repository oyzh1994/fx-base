package cn.oyzh.fx.pkg.jdeps;

import cn.oyzh.fx.pkg.ConfigParser;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;
import java.util.List;

/**
 * jdeps配置解析器
 *
 * @author oyzh
 * @since 2024/6/17
 */
public class JDepsConfigParser implements ConfigParser<JDepsConfig> {

    @Override
    public JDepsConfig parse(JSONObject object) {
        JDepsConfig config = new JDepsConfig();
        JSONArray arr1 = object.getJSONArray("skips");
        List<String> skips = arr1.toJavaList(String.class);
        if (skips != null) {
            config.setSkips(new HashSet<>(skips));
        }
        JSONArray arr2 = object.getJSONArray("excludes");
        List<String> excludes = arr2.toJavaList(String.class);
        if (excludes != null) {
            config.setExcludes(new HashSet<>(excludes));
        }
        Integer multiRelease = object.getInteger("multi-release");
        if (multiRelease != null) {
            config.setMultiRelease(multiRelease);
        }
        Boolean enable = object.getBoolean("enable");
        if (enable != null) {
            config.setEnable(enable);
        }
        Boolean summary = object.getBoolean("summary");
        if (summary != null) {
            config.setSummary(summary);
        }
        Boolean verbose = object.getBoolean("verbose");
        if (verbose != null) {
            config.setVerbose(verbose);
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
