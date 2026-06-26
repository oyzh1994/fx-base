package cn.oyzh.fx.pkg.jre;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import com.alibaba.fastjson2.JSONObject;

import java.util.HashSet;
import java.util.List;

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
        List<String> excludes = JSONUtil.toList(object, "excludes", String.class);
        if (excludes != null) {
            config.setExcludes(new HashSet<>(excludes));
        }
        Boolean enable = object.getBoolean("enable");
        if (enable != null) {
            config.setEnable(enable);
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
