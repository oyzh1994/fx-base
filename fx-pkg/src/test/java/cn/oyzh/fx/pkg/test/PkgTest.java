// package cn.oyzh.fx.pkg.test;
//
// import cn.hutool.core.util.ZipUtil;
// import cn.oyzh.fx.pkg.config.ConfigParser;
// import cn.oyzh.fx.pkg.config.PlatformConfig;
// import cn.oyzh.fx.pkg.packager.BasePackager;
// import cn.oyzh.fx.pkg.util.PkgUtil;
// import org.junit.Test;
//
// /**
//  * @author oyzh
//  * @since 2023/3/8
//  */
// public class PkgTest {
//
//     @Test
//     public void pkg_test() throws Exception {
//         String global_config = "global_config.json";
//         String win_pkg_config = "win_pkg_config.json";
//         String linux_pkg_config = "linux_pkg_config.json";
//         String macos_pkg_config = "macos_pkg_config.json";
//         ConfigParser parser = new ConfigParser();
//         parser.loadConfig(global_config, win_pkg_config, linux_pkg_config, macos_pkg_config);
//         for (String platform : parser.getPlatforms()) {
//             PlatformConfig platformConfig = parser.getCrossPlatformConfig(platform);
//             BasePackager packager = PkgUtil.getPackager(platform);
//             packager.setPlatformConfig(platformConfig);
//             packager.setGlobalConfig(parser.getGlobalConfig());
//             packager.pack();
//         }
//     }
//
//     @Test
//     public void pkg_win_test() throws Exception {
//         String global_config = "global_config.json";
//         String win_pkg_config = "win_pkg_config.json";
//         ConfigParser parser = new ConfigParser();
//         parser.loadConfig(global_config, win_pkg_config);
//         for (String platform : parser.getPlatforms()) {
//             PlatformConfig platformConfig = parser.getCrossPlatformConfig(platform);
//             BasePackager packager = PkgUtil.getPackager(platform);
//             packager.setPlatformConfig(platformConfig);
//             packager.setGlobalConfig(parser.getGlobalConfig());
//             packager.pack();
//         }
//     }
//
//     @Test
//     public void pkg_linux_test() throws Exception {
//         String global_config = "global_config.json";
//         String linux_pkg_config = "linux_pkg_config.json";
//         ConfigParser parser = new ConfigParser();
//         parser.loadConfig(global_config, linux_pkg_config);
//         for (String platform : parser.getPlatforms()) {
//             PlatformConfig platformConfig = parser.getCrossPlatformConfig(platform);
//             BasePackager packager = PkgUtil.getPackager(platform);
//             packager.setPlatformConfig(platformConfig);
//             packager.setGlobalConfig(parser.getGlobalConfig());
//             packager.pack();
//         }
//     }
//
//     @Test
//     public void pkg_macos_test() throws Exception {
//         String global_config = "global_config.json";
//         String linux_pkg_config = "macos_pkg_config.json";
//         ConfigParser parser = new ConfigParser();
//         parser.loadConfig(global_config, linux_pkg_config);
//         for (String platform : parser.getPlatforms()) {
//             PlatformConfig platformConfig = parser.getCrossPlatformConfig(platform);
//             BasePackager packager = PkgUtil.getPackager(platform);
//             packager.setPlatformConfig(platformConfig);
//             packager.setGlobalConfig(parser.getGlobalConfig());
//             packager.pack();
//         }
//     }
//
//     @Test
//     public void pkg_test2() throws Exception {
//         ZipUtil.zip("D:\\Package\\EasyZK\\EasyZK\\EasyZK", "D:\\Package\\EasyZK\\EasyZK.zip", false);
//     }
//
//     @Test
//     public void pkg_test3() throws Exception {
//         String global_config = "global_config.json";
//         String macos_pkg_config = "macos_pkg_config.json";
//         ConfigParser parser = new ConfigParser();
//         parser.loadConfig(global_config, macos_pkg_config);
//         for (String platform : parser.getPlatforms()) {
//             PlatformConfig platformConfig = parser.getCrossPlatformConfig(platform);
//             String cmd = PkgUtil.getJLinkCMD(platformConfig.getJLinkConfig());
//             System.out.println(cmd);
//         }
//     }
//
//     @Test
//     public void pkg_test4() throws Exception {
//         String global_config = "global_config.json";
//         String linux_pkg_config = "linux_pkg_config.json";
//         ConfigParser parser = new ConfigParser();
//         parser.loadConfig(global_config, linux_pkg_config);
//         for (String platform : parser.getPlatforms()) {
//             PlatformConfig platformConfig = parser.getCrossPlatformConfig(platform);
//             String cmd = PkgUtil.getJLinkCMD(platformConfig.getJLinkConfig());
//             System.out.println(cmd);
//         }
//     }
//
// }
