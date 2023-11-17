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
    public JreClipConfig cross(Object o) {
        if (o instanceof JreClipConfig config) {
            JreClipConfig config1 = new JreClipConfig();
            config1.setEnable(config.isEnable());
            config1.excludeFiles.addAll(this.excludeFiles);
            config1.excludeFiles.addAll(config.excludeFiles);
            if (config.src != null) {
                config1.src = config.src;
            } else {
                config1.src = this.src;
            }
            if (config.dest != null) {
                config1.dest = config.dest;
            } else {
                config1.dest = this.dest;
            }
            return config1;
        }
        return this;
    }

}
