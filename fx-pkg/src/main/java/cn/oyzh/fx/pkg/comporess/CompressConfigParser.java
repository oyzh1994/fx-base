package cn.oyzh.fx.pkg.comporess;

import cn.hutool.json.JSONObject;
import cn.oyzh.fx.pkg.ConfigParser;

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
        String name = object.getStr("name");
        if (name != null) {
            config.setName(name);
        }
        String type = object.getStr("type");
        if (type != null) {
            config.setType(type);
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
