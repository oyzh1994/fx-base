// package cn.oyzh.fx.pkg.jpackage;
//
// import cn.oyzh.fx.common.util.RuntimeUtil;
// import cn.oyzh.fx.pkg.util.PkgUtil;
// import lombok.NonNull;
//
// /**
//  * jpackage处理
//  *
//  * @author oyzh
//  * @since 2023/3/8
//  */
// public class JPackageHandler {
//
//     /**
//      * 执行jlink
//      *
//      * @param config  配置
//      * @param jdkPath jdk路径
//      */
//     public void exec(@NonNull JPackageConfig config, String jdkPath) throws Exception {
//         String cmdStr = PkgUtil.getJPackageCMD(config);
//         cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
//         // 执行jpackage
//         RuntimeUtil.execAndWait(cmdStr);
//     }
// }
