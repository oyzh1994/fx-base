// package cn.oyzh.fx.pkg.packager;
//
// import cn.oyzh.fx.common.util.OSUtil;
// import cn.oyzh.fx.pkg.packr.PackrConfig;
//
// /**
//  * windows平台打包器
//  *
//  * @author oyzh
//  * @since 2023/3/8
//  */
// public class WinPackager extends BasePackager {
//
//     @Override
//     protected void packBefore() throws Exception {
//         if (!OSUtil.isWindows()) {
//             throw new UnsupportedOperationException("current os is:" + OSUtil.getOSType() + ", cat not jlink windows jre!");
//         }
//         super.packBefore();
//     }
//
//     @Override
//     public void pack() throws Exception {
//         this.packBefore();
//         if (OSUtil.isWindows()) {
//             super.packByJPackage();
//         } else {
//             super.packByPackr();
//         }
//         this.packAfter();
//     }
//
//     @Override
//     protected PackrConfig.Platform getPlatform() {
//         return PackrConfig.Platform.Windows64;
//     }
// }
