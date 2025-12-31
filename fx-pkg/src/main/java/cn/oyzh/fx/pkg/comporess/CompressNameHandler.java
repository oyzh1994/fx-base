// package cn.oyzh.fx.pkg.comporess;
//
// import cn.oyzh.common.system.OSUtil;
// import cn.oyzh.fx.pkg.PackOrder;
// import cn.oyzh.fx.pkg.PostHandler;
// import cn.oyzh.fx.pkg.config.PackConfig;
//
// /**
//  * 压缩名称处理器
//  *
//  * @author oyzh
//  * TODO: 由以下类替代
//  * @see cn.oyzh.fx.pkg.pack.DestHandler
//  * @since 2024/06/18
//  */
// @Deprecated
// public class CompressNameHandler implements PostHandler {
//
//     private int order = PackOrder.ORDER_M5;
//
//     public int order() {
//         return order;
//     }
//
//     public void order(int order) {
//         this.order = order;
//     }
//
//     @Override
//     public void handle(PackConfig packConfig) throws Exception {
//         CompressConfig compressConfig = packConfig.getCompressConfig();
//         if (compressConfig == null) {
//             return;
//         }
//         if (compressConfig.getName() != null) {
//             return;
//         }
//         String compressName = "";
//         if (packConfig.getAppName() != null) {
//             compressName += packConfig.getAppName();
//         }
//         if (packConfig.appVersion() != null) {
//             compressName = compressName + "_v" + packConfig.appVersion();
//         }
//         if (packConfig.getPlatform() != null) {
//             compressName = compressName + "_" + packConfig.getPlatform();
//         }
//         compressName = compressName + "_" + OSUtil.getArchName();
//         // compressName += "_" + new SimpleDateFormat("yyyyMMdd").format(new Date());
//         if (packConfig.getBuildType() != null) {
//             compressName = compressName + "_" + packConfig.getBuildType();
//             // } else {
//             //     compressName = compressName + "_release";
//         }
//         compressConfig.setName(compressName);
//     }
//
//     @Override
//     public String name() {
//         return "压缩文件名处理";
//     }
// }
