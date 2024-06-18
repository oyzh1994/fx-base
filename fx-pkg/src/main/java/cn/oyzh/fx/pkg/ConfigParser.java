package cn.oyzh.fx.pkg;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * 配置解析器
 *
 * @author oyzh
 * @since 2024/6/14
 */
public interface ConfigParser<C> {

    /**
     * 解析配置
     *
     * @param configFile 配置文件
     * @return 配置类
     */
    default C parse(String configFile) {
        JSONObject object = JSONUtil.parseObj(FileUtil.readUtf8String(configFile));
        return this.parse(object);
    }

    /**
     * 解析配置
     *
     * @param object 配置内容
     * @return 配置类
     */
    C parse(JSONObject object);
}
