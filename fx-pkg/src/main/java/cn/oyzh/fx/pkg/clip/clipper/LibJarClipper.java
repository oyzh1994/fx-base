// package cn.oyzh.fx.pkg.clip.clipper;
//
// import cn.hutool.core.io.FileUtil;
// import cn.hutool.log.StaticLog;
// import cn.oyzh.fx.pkg.util.JarUtil;
// import lombok.NonNull;
//
// import java.io.File;
// import java.util.List;
//
// /**
//  * 类库jar裁剪器
//  *
//  * @author oyzh
//  * @since 2022/12/14
//  */
// //@Slf4j
// public class LibJarClipper extends BaseJarClipper {
//
//     /**
//      * 裁剪类库jar
//      *
//      * @param jarUnDir 主jar解压目录
//      */
//     public void clipLibs(@NonNull String jarUnDir) {
//         if (!FileUtil.exist(jarUnDir)) {
//             throw new RuntimeException("jarUnDir " + jarUnDir + " is not exist.");
//         }
//         if (!FileUtil.isDirectory(jarUnDir)) {
//             throw new RuntimeException("jarUnDir " + jarUnDir + " is not dir.");
//         }
//         JulLog.info("clipLibs start, jarUnDir: {}.", jarUnDir);
//         List<File> files = FileUtil.loopFiles(jarUnDir);
//         for (File file : files) {
//             try {
//                 // 非jar，跳过
//                 if (!JarUtil.isJar(file)) {
//                     continue;
//                 }
//                 // 符合排除jar，删除文件
//                 if (this.jarFilter.acceptExclude(file.getName())) {
//                     FileUtil.del(file);
//                     continue;
//                 }
//                 // 替换路径
//                 String dest = file.getPath().replace("\\", "/");
//                 JulLog.info("clipLibJar jar: {}.", file.getName());
//                 // 文件名
//                 dest = dest.replace(".jar", "_clip.jar");
//                 // 裁剪类库
//                 this.clip(file.getPath(), dest);
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//             }
//         }
//         JulLog.info("clipLibs finish.");
//     }
//
//     /**
//      * 覆盖类库jar
//      *
//      * @param jarUnDir 主jar解压目录
//      */
//     public void coverLibs(@NonNull String jarUnDir) {
//         if (!FileUtil.exist(jarUnDir)) {
//             throw new RuntimeException("jarUnDir " + jarUnDir + " is not exist.");
//         }
//         if (!FileUtil.isDirectory(jarUnDir)) {
//             throw new RuntimeException("jarUnDir " + jarUnDir + " is not dir.");
//         }
//         JulLog.info("coverLibs start, jarUnDir: {}.", jarUnDir);
//         List<File> files = FileUtil.loopFiles(jarUnDir);
//         for (File file : files) {
//             try {
//                 if (JarUtil.isJar(file) && file.getName().endsWith("_clip.jar")) {
//                     // 覆盖旧类库
//                     String newName = file.getName().replace("_clip", "");
//                     FileUtil.rename(file, newName, true);
//                 }
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//             }
//         }
//         JulLog.info("coverLibs finish.");
//     }
//
//     /**
//      * 删除空jar
//      *
//      * @param jarUnDir 主jar解压目录
//      */
//     public void delEmptyLibs(@NonNull String jarUnDir) {
//         if (!FileUtil.exist(jarUnDir)) {
//             throw new RuntimeException("jarUnDir " + jarUnDir + " is not exist.");
//         }
//         if (!FileUtil.isDirectory(jarUnDir)) {
//             throw new RuntimeException("jarUnDir " + jarUnDir + " is not dir.");
//         }
//         JulLog.info("delEmptyLibs start, jarUnDir: {}.", jarUnDir);
//         List<File> files = FileUtil.loopFiles(jarUnDir);
//         for (File file : files) {
//             try {
//                 // 非jar，跳过
//                 if (!JarUtil.isJar(file)) {
//                     continue;
//                 }
//                 // 没有class文件则删除此jar
//                 if (!JarUtil.hasClass(file.getPath())) {
//                     FileUtil.del(file);
//                     JulLog.warn("lib {} is empty, deleted.", file.getName());
//                 }
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//             }
//         }
//         JulLog.info("delEmptyLibs finish.");
//     }
// }
