package cn.oyzh.fx.pkg.clip.clipper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * jre裁剪配置
 *
 * @author oyzh
 * @since 2023/03/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JreClipConfig extends BaseClipConfig {

    /**
     * 从配置文件生成配置对象
     *
     * @param configPath 配置文件路径
     * @return 配置对象
     */
    public static JreClipConfig fromConfig(String configPath) {
        JreClipConfig config = new JreClipConfig();
        config.parseConfig(configPath);
        config.applyDefault();
        return config;
    }
}
