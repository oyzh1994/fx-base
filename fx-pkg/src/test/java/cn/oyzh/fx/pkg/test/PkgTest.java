package cn.oyzh.fx.pkg.test;

import cn.hutool.core.util.ZipUtil;
import cn.oyzh.fx.pkg.config.ConfigParser;
import cn.oyzh.fx.pkg.config.PlatformConfig;
import cn.oyzh.fx.pkg.packager.BasePackager;
import cn.oyzh.fx.pkg.util.PkgUtil;
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
            PlatformConfig platformConfig = parser.getCrossPlatformConfig(platform);
            BasePackager packager = PkgUtil.getPackager(platform);
            packager.setPlatformConfig(platformConfig);
            packager.setGlobalConfig(parser.getGlobalConfig());
            packager.pack();
        }
    }

    @Test
    public void pkg_test2() throws Exception {
        ZipUtil.zip("D:\\Package\\EasyZK\\EasyZK\\EasyZK", "D:\\Package\\EasyZK\\EasyZK.zip", false);

    }

}
