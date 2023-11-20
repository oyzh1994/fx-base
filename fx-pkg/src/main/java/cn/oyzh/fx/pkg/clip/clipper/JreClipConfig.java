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

    @Override
    public JreClipConfig clone() {
        JreClipConfig config = new JreClipConfig();
        config.src = this.src;
        config.dest = this.dest;
        config.enable = this.enable;
        config.excludeFiles = this.excludeFiles;
        return config;
    }

    @Override
    public JreClipConfig cross(Object o) {
        JreClipConfig config1 = this.clone();
        if (o instanceof JreClipConfig config) {
            config1.enable = config.enable;
            if (config.excludeFiles != null) {
                if (config1.excludeFiles == null) {
                    config1.excludeFiles = config.excludeFiles;
                } else {
                    config1.excludeFiles.addAll(config.excludeFiles);
                }
            }
            if (config.src != null) {
                config1.src = config.src;
            }
            if (config.dest != null) {
                config1.dest = config.dest;
            }
        }
        return config1;
    }

}
