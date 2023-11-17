package cn.oyzh.fx.pkg.test;

import cn.hutool.core.util.ZipUtil;
import cn.oyzh.fx.pkg.clip.clipper.JarClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JreClipConfig;
import cn.oyzh.fx.pkg.config.ConfigParser;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.jlink.JLinkHandler;
import cn.oyzh.fx.pkg.packager.PackageConfig;
import cn.oyzh.fx.pkg.packager.WinPackager;
import org.junit.Test;

/**
 * @author oyzh
 * @since 2023/3/8
 */
public class PkgTest {

    @Test
    public void pkg_test() throws Exception {
        String config = "/pkg_config.yml";
        ConfigParser parser = new ConfigParser();
        parser.parseConfig(config);
        for (String platform : parser.getPlatforms()) {
            PackageConfig packageConfig= parser.getCrossPackageConfig(platform);
            JLinkConfig jLinkConfig= parser.getCrossJLinkConfig(platform);
            JreClipConfig jreClipConfig= parser.getCrossJreClipConfig(platform);
            JarClipConfig jarClipConfig= parser.getCrossJarClipConfig(platform);
            WinPackager packager = new WinPackager();
            packager.setPackageConfig(packageConfig);
            packager.setJLinkConfig(jLinkConfig);
            packager.setJreClipConfig(jreClipConfig);
            packager.setJarClipConfig(jarClipConfig);
            packager.setGlobalConfig(parser.getGlobalConfig());

            packager.pack();
        }
    }

    @Test
    public void pkg_test2() throws Exception {
        ZipUtil.zip("D:\\Package\\EasyZK\\EasyZK\\EasyZK", "D:\\Package\\EasyZK\\EasyZK.zip", false);

    }

}
