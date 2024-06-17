package cn.oyzh.fx.pkg.jre;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.jar.JarConfig;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author oyzh
 * @since 2024/6/17
 */
public class JreConfigParser implements ConfigParser<JreConfig> {

    @Override
    public JreConfig parse(String configFile) {
        JSONObject object = JSONUtil.parseObj(FileUtil.readUtf8String(configFile));
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

    public static JreConfig parseConfig(String configFile) {
        return new JreConfigParser().parse(configFile);
    }
}
