package cn.oyzh.fx.pkg.config;

import cn.oyzh.fx.pkg.clip.clipper.JarClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JreClipConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.packager.PackageConfig;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2023/11/17
 */
@Getter
@Setter
public class PlatformConfig extends BaseConfig {

    /**
     * jlink配置
     */
    private  JLinkConfig jLinkConfig = new JLinkConfig();

    /**
     * jar裁剪配置
     */
    private  JarClipConfig jarClipConfig = new JarClipConfig();

    /**
     * jre裁剪配置
     */
    private  JreClipConfig jreClipConfig = new JreClipConfig();

    /**
     * 打包配置
     */
    private  PackageConfig packageConfig = new PackageConfig();

    @Override
    public void parseConfig(JSONObject object1) {
        super.parseConfig(object1);
        if (object1.containsKey("package")) {
            JSONObject object = object1.getJSONObject("package");
            this.packageConfig.parseConfig(object);
        }
        if (object1.containsKey("jlink")) {
            JSONObject object = object1.getJSONObject("jlink");
            this.jLinkConfig.parseConfig(object);
        }
        if (object1.containsKey("jre_clip")) {
            JSONObject object = object1.getJSONObject("jre_clip");
            this.jreClipConfig.parseConfig(object);
        }
        if (object1.containsKey("jar_clip")) {
            JSONObject object = object1.getJSONObject("jar_clip");
            this.jarClipConfig.parseConfig(object);
        }
    }
}
