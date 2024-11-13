// package cn.oyzh.fx.pkg.clip.clipper;
//
// import cn.hutool.core.io.FileUtil;
// import cn.hutool.log.StaticLog;
// import cn.oyzh.common.util.RuntimeUtil;
// import cn.oyzh.fx.pkg.util.PkgUtil;
//
// import java.io.File;
// import java.util.List;
// import java.util.stream.Collectors;
//
// /**
//  * 主jar裁剪器
//  *
//  * @author oyzh
//  * @since 2022/12/14
//  */
// //@Slf4j
// public class MainJarClipper extends BaseJarClipper {
//
//     /**
//      * 合并类库jar到主jar
//      *
//      * @param jarUnDir 主jar解压目录
//      * @param mainJar  主jar
//      * @param jdkPath  jdk路径
//      */
//     public void mergeLibs(String jarUnDir, String mainJar, String jdkPath) {
//         if (!FileUtil.exist(jarUnDir)) {
//             throw new RuntimeException("jarUnDir " + jarUnDir + " is not exist.");
//         }
//         if (!FileUtil.isDirectory(jarUnDir)) {
//             throw new RuntimeException("jarUnDir " + jarUnDir + " is not dir.");
//         }
//         if (!FileUtil.exist(mainJar)) {
//             throw new RuntimeException("mainJar " + mainJar + " is not exist.");
//         }
//         if (!FileUtil.isFile(mainJar)) {
//             throw new RuntimeException("mainJar " + mainJar + " is not file.");
//         }
//         JulLog.info("mergeLibs start, jarUnDir: {} mainJar: {}.", jarUnDir, mainJar);
//         // jar文件
//         File mainJarFile = new File(mainJar);
//         // 新jar文件
//         File mainJarNewFile = new File(jarUnDir, mainJarFile.getName());
//         // 移动到解压目录
//         if (mainJarNewFile.exists()) {
//             FileUtil.move(mainJarFile, mainJarNewFile, true);
//         } else {
//             FileUtil.copy(mainJarFile, mainJarNewFile, false);
//         }
//         // 解压目录
//         File dir = new File(jarUnDir);
//         // 移动主jar文件到解压目录
//         // Runtime.getRuntime().exec("cmd cd " + jarUnDir);
//         // RuntimeUtil.execAndWait("cmd cd " + jarUnDir);
//         // lib目录合并
//         if (FileUtil.exist(jarUnDir + "/BOOT-INF/lib")) {
//             try {
//                 // 合并lib目录到主jar文件
//                 String cmdStr = "jar -uvf0 " + mainJarFile.getName() + " ./BOOT-INF/lib";
//                 cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
//                 RuntimeUtil.execAndWait(cmdStr, dir);
//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//         } else {// 单个jar逐个合并
//             List<File> files = FileUtil.loopFiles(dir);
//             files = files.parallelStream().filter(f -> f.isFile() && f.getName().endsWith(".jar")).collect(Collectors.toList());
//             for (File file : files) {
//                 try {
//                     String fName = file.getPath().replace(dir.getPath(), "");
//                     String cmdStr = "jar -uvf0 " + mainJarFile.getName() + " ." + fName;
//                     cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
//                     RuntimeUtil.execAndWait(cmdStr, dir);
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//             }
//         }
//         // 移动主jar文件到原始目录
//         FileUtil.move(mainJarNewFile, mainJarFile, true);
//         JulLog.info("mergeLibs finish.");
//     }
// }
