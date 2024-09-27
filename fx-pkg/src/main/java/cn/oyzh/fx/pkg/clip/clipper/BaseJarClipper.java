// package cn.oyzh.fx.pkg.clip.clipper;
//
// import cn.hutool.core.io.FileUtil;
// import cn.hutool.core.io.IoUtil;
// import cn.hutool.log.StaticLog;
// import cn.oyzh.fx.pkg.filter.ClassFilter;
// import cn.oyzh.fx.pkg.filter.JarFilter;
// import lombok.Getter;
// import lombok.NonNull;
// import lombok.experimental.Accessors;
//
// import java.io.BufferedInputStream;
// import java.io.BufferedOutputStream;
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.util.Set;
// import java.util.jar.JarInputStream;
// import java.util.jar.JarOutputStream;
// import java.util.jar.Manifest;
// import java.util.zip.ZipEntry;
//
// /**
//  * 基础的jar裁剪器
//  *
//  * @author oyzh
//  * @since 2023/3/9
//  */
// //@Slf4j
// public class BaseJarClipper extends BaseClipper {
//
//     /**
//      * jar过滤器
//      */
//     @Getter
//     @Accessors(chain = true, fluent = true)
//     protected final JarFilter jarFilter = new JarFilter();
//
//     /**
//      * 类过滤器
//      */
//     @Getter
//     @Accessors(chain = true, fluent = true)
//     protected final ClassFilter classFilter = new ClassFilter();
//
//     @Override
//     public void clip(@NonNull String src, String dest) throws Exception {
//         super.clip(src, dest);
//         JulLog.info("clipJar start, src:{}", src);
//         JarInputStream jarIn = new JarInputStream(new BufferedInputStream(new FileInputStream(src)));
//         Manifest manifest = jarIn.getManifest();
//         JarOutputStream jarOut;
//         if (manifest == null) {
//             jarOut = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(dest)));
//         } else {
//             jarOut = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(dest)), manifest);
//         }
//         try {
//             byte[] bytes = new byte[1024];
//             while (true) {
//                 //重点
//                 ZipEntry entry = jarIn.getNextJarEntry();
//                 if (entry == null) {
//                     break;
//                 }
//                 String name = entry.getName();
//                 // 执行过滤
//                 if (this.filterName(name)) {
//                     // 添加到新jar文件
//                     jarOut.putNextEntry(entry);
//                     int len = jarIn.read(bytes, 0, bytes.length);
//                     while (len != -1) {
//                         jarOut.write(bytes, 0, len);
//                         len = jarIn.read(bytes, 0, bytes.length);
//                     }
//                 }
//             }
//         } finally {
//             IoUtil.close(jarIn);
//             jarOut.finish();
//             IoUtil.close(jarOut);
//         }
//         JulLog.info("clipJar finish dest:{}", dest);
//     }
//
//     @Override
//     public boolean filterName(String name) {
//         boolean accept;
//         // class文件
//         if (name.endsWith(".class")) {
//             accept = this.classFilter.accept(name);
//         } else if (name.endsWith(".jar")) { // jar包不处理
//             accept = false;
//         } else {// 其他文件
//             accept = super.fileFilter.accept(name);
//         }
//         if (!accept) {
//             JulLog.info("file:{} filtered.", name);
//         }
//         return accept;
//     }
//
//     /**
//      * 添加排除的jar名称
//      *
//      * @param excludeJars 排除的jar名称列表
//      */
//     public void addExcludeJars(Set<String> excludeJars) {
//         this.jarFilter.addExcludes(excludeJars);
//     }
//
//     /**
//      * 添加排除的class名称
//      *
//      * @param excludeClasses 排除的class名称列表
//      */
//     public void addExcludeClasses(Set<String> excludeClasses) {
//         this.classFilter.addExcludes(excludeClasses);
//     }
//
//     @Override
//     protected void checkPath(String src, String dest) {
//         // 检查src
//         if (!FileUtil.exist(src) || !FileUtil.isFile(src)) {
//             throw new RuntimeException("src " + src + " is invalid!");
//         }
//
//         // 检查dest
//         if (FileUtil.exist(dest)) {
//             if (!FileUtil.isFile(dest)) {
//                 throw new RuntimeException("dest " + dest + " exist but not file!");
//             }
//             if (new File(src).getPath().equals(new File(dest).getPath())) {
//                 throw new RuntimeException("src and dest is same file!");
//             }
//         }
//     }
// }
