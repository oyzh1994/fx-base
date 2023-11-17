package cn.oyzh.fx.pkg.config;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * 基础配置对象
 *
 * @author oyzh
 * @since 2023/11/15
 */
@Data
public class BaseConfig {

    /**
     * 是否启用
     */
    private boolean enable = true;

    /**
     * 解析配置
     *
     * @param object 配置对象
     */
    public void parseConfig(JSONObject object) {
        Boolean enable = object.getBoolean("enable");
        if (enable != null) {
            this.enable = enable;
        }
    }

    /**
     * 配置交叉
     *
     * @param o 配置对象
     * @return 新配置对象
     */
    public BaseConfig cross(Object o) {
        return null;
    }
}
