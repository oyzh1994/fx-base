package cn.oyzh.fx.pkg.packager;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * windows打包配置
 *
 * @author oyzh
 * @since 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WinPkgConfig extends BasePkgConfig {

    @Override
    protected void fromConfig(JSONObject object) {
        super.fromConfig(object);
    }

    /**
     * 从配置文件生成配置对象
     *
     * @param configPath 配置文件路径
     * @return 配置对象
     */
    public static WinPkgConfig fromConfig(String configPath) {
        WinPkgConfig config = new WinPkgConfig();
        config.parseConfig(configPath);
        return config;
    }
}
