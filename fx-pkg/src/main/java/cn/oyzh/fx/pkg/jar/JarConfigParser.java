package cn.oyzh.fx.pkg.jar;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import com.alibaba.fastjson2.JSONObject;

import java.util.HashSet;
import java.util.List;

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
        List<String> excludes = JSONUtil.toList(object, "excludes", String.class);
        if (excludes != null) {
            config.setExcludes(new HashSet<>(excludes));
        }
        List<String> skipsJar = JSONUtil.toList(object, "skipsJar", String.class);
        if (skipsJar != null) {
            config.setSkipsJar(new HashSet<>(skipsJar));
        }
        Boolean enable = object.getBoolean("enable");
        if (enable != null) {
            config.setEnable(enable);
        }
        Boolean removeEmpty = object.getBoolean("removeEmpty");
        if (removeEmpty != null) {
            config.setRemoveEmpty(removeEmpty);
        }
        return config;
    }

    public static JarConfig parseConfig(JSONObject object) {
        return new JarConfigParser().parse(object);
    }

    public static JarConfig parseConfig(String configFile) {
        return new JarConfigParser().parse(configFile);
    }
}
