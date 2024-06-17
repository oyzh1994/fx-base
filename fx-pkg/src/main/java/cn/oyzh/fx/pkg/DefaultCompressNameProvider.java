// package cn.oyzh.fx.pkg;
//
// import cn.oyzh.fx.pkg.packager.PackageConfig;
//
// import java.text.SimpleDateFormat;
// import java.util.Date;
//
// /**
//  * @author oyzh
//  * @since 2024/4/2
//  */
// public class DefaultCompressNameProvider implements CompressNameProvider {
//
//     @Override
//     public String getCompressName(PackageConfig packageConfig) {
//         String name = "";
//         if (packageConfig.getAppName() != null) {
//             name += packageConfig.getAppName();
//         }
//         if (packageConfig.getVersion() != null) {
//             name = name + "_" + packageConfig.getVersion();
//         }
//         if (packageConfig.getPlatform() != null) {
//             name = name + "_" + packageConfig.getPlatform();
//         }
//         name += "_" + new SimpleDateFormat("yyyyMMdd").format(new Date());
//         if (packageConfig.getBuildType() != null) {
//             name = name + "_" + packageConfig.getBuildType();
//         } else {
//             name = name + "_release";
//         }
//         return name;
//     }
// }
