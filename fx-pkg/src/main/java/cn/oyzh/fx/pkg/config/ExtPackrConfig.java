// package cn.oyzh.fx.pkg.config;
//
// import cn.oyzh.fx.pkg.packr.Packr;
// import cn.oyzh.fx.pkg.packr.PackrConfig;
// import lombok.Getter;
// import lombok.Setter;
//
// import java.io.File;
// import java.util.HashMap;
// import java.util.Map;
//
// /**
//  * @author oyzh
//  * @since 2024/6/14
//  */
// @Getter
// public class ExtPackrConfig extends PackrConfig {
//
//     /**
//      * jlink后的jre目录
//      */
//     @Setter
//     private String jlinkJre;
//
//     /**
//      * 最小化后的jre目录
//      */
//     @Setter
//     private String minimizeJre;
//
//     /**
//      * jar解压目录
//      */
//     @Setter
//     @Getter
//     private String jarUnDir;
//
//     /**
//      * 最小化后的主程序
//      */
//     @Setter
//     @Getter
//     private String minimizeManJar;
//
//     /**
//      * 主程序
//      */
//     @Setter
//     private String mainJar;
//
//     /**
//      * 版本
//      */
//     @Setter
//     private String version;
//
//     /**
//      * 构建类型
//      */
//     @Setter
//     private String buildType;
//
//     /**
//      * 压缩类型
//      */
//     @Getter
//     @Setter
//     private String compressType;
//
//     /**
//      * 压缩文件名称
//      */
//     @Getter
//     @Setter
//     private String compressName;
//
//     /**
//      * 最终压缩文件
//      */
//     @Getter
//     @Setter
//     private File compressFile;
//
//     /**
//      * 执行用的jdk路径
//      */
//     @Getter
//     @Setter
//     private String jdkExec;
//
//     /**
//      * 属性
//      */
//     private final Map<String, Object> properties = new HashMap<>();
//
//     public void putProperty(String key, Object value) {
//         this.properties.put(key, value);
//     }
//
//     public Object getProperty(String key) {
//         return this.properties.get(key);
//     }
//
//     public String jrePath() {
//         if (this.jrePath != null) {
//             return Packr.DEFAULT_JRE_PATH;
//         }
//         return this.jrePath;
//     }
//
//     public String mainJar() {
//         if (this.minimizeManJar != null) {
//             return this.minimizeManJar;
//         }
//         return this.mainJar;
//     }
//
//     public String mainJarName() {
//         String mainJar = this.mainJar();
//         if (mainJar.contains("/")) {
//             return mainJar.substring(mainJar.lastIndexOf("/") + 1);
//         }
//         if (mainJar.contains("\\")) {
//             return mainJar.substring(mainJar.lastIndexOf("\\") + 1);
//         }
//         return mainJar;
//     }
//
//     public String outPath() {
//         if (this.compressFile != null) {
//             return this.compressFile.getPath();
//         }
//         return this.outDir.getPath();
//     }
//
//     public String jdkExec() {
//         if (this.jdkExec == null) {
//             return this.jdk;
//         }
//         return this.jdkExec;
//     }
// }
