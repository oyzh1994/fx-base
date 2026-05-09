package cn.oyzh.fx.pkg.comporess;

import cn.oyzh.fx.pkg.ConfigParser;
import com.alibaba.fastjson.JSONObject;

/**
 * 压缩配置解析器
 *
 * @author oyzh
 * @since 2024/06/18
 */
public class CompressConfigParser implements ConfigParser<CompressConfig> {

    @Override
    public CompressConfig parse(JSONObject object) {
        CompressConfig config = new CompressConfig();
        if (object.containsKey("name")) {
            config.setName(object.getString("name"));
        }
        if (object.containsKey("type")) {
            config.setType(object.getString("type"));
        }
        return config;
    }

    public static CompressConfig parseConfig(JSONObject object) {
        return new CompressConfigParser().parse(object);
    }

    public static CompressConfig parseConfig(String configFile) {
        return new CompressConfigParser().parse(configFile);
    }
}
