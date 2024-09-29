// package cn.oyzh.fx.pkg.packager;
//
// import cn.hutool.core.util.StrUtil;
// import cn.hutool.json.JSONObject;
// import cn.oyzh.fx.common.util.FileNameUtil;
// import cn.oyzh.fx.pkg.config.BaseConfig;
// import lombok.Data;
// import lombok.EqualsAndHashCode;
//
// import java.io.File;
//
// /**
//  * 打包配置
//  *
//  * @author oyzh
//  * @since 2023/11/14
//  */
// @Data
// @EqualsAndHashCode(callSuper = true)
// public class PackageConfig extends BaseConfig {
//
//     /**
//      * 程序描述
//      */
//     private String desc;
//
//     /**
//      * 作者
//      */
//     private String vendor;
//
//     /**
//      * 程序名称
//      */
//     private String appName;
//
//     /**
//      * 程序logo
//      */
//     private String appIcon;
//
//     /**
//      * 平台
//      */
//     private String platform;
//
//     /**
//      * 构建类型
//      */
//     private String buildType;
//
//     /**
//      * 主jar路径
//      */
//     private String jarPath;
//
//     /**
//      * jre路径
//      */
//     private String jrePath;
//
//     /**
//      * 运行的主类
//      */
//     private String mainClass;
//
//     /**
//      * 目标路径
//      */
//     private String destPath;
//
//     /**
//      * 版本号
//      */
//     private String version;
//
//     /**
//      * 可执行程序的名称
//      */
//     private String executable;
//
//     /**
//      * windows
//      * app-image|exe|msi
//      * <p>
//      * linux
//      * app-image|rpm|deb
//      * <p>
//      * mac
//      * app-image
//      */
//     private String type = "app-image";
//
//     /**
//      * 压缩类型
//      * <p>
//      * zip|tar.gz
//      */
//     private String compressType;
//
//     /**
//      * 程序唯一标识符(仅macos)
//      */
//     private String identifier;
//
//     /**
//      * 获取目标文件
//      *
//      * @return 目标文件
//      */
//     public File getDestFile() {
//         if (this.destPath == null) {
//             return null;
//         }
//         return new File(this.destPath);
//     }
//
//     /**
//      * 获取app目标目录
//      *
//      * @return app目标目录
//      */
//     public String getAppDest() {
//         if (StringUtil.startWithIgnoreCase(this.platform, "macos")) {
//             return FileNameUtil.concat(this.destPath, this.appName + ".app");
//         }
//         return FileNameUtil.concat(this.destPath, this.appName);
//     }
//
//     /**
//      * 获取icon文件
//      *
//      * @return icon文件
//      */
//     public File getIconFile() {
//         if (this.appIcon == null) {
//             return null;
//         }
//         return new File(this.appIcon);
//     }
//
//     /**
//      * 获取主jar文件
//      *
//      * @return 主jar文件
//      */
//     public File getJarFile() {
//         if (this.jarPath == null) {
//             return null;
//         }
//         return new File(this.jarPath);
//     }
//
//     @Override
//     public void parseConfig(JSONObject object) {
//         super.parseConfig(object);
//         String identifier = object.getStr("identifier");
//         if (identifier != null) {
//             this.identifier = identifier;
//         }
//         String type = object.getStr("type");
//         if (type != null) {
//             this.type = type;
//         }
//         String buildType = object.getStr("buildType");
//         if (buildType != null) {
//             this.buildType = buildType;
//         }
//         String compressType = object.getStr("compressType");
//         if (compressType != null) {
//             this.compressType = compressType;
//         }
//         String version = object.getStr("version");
//         if (version != null) {
//             this.version = version;
//         }
//         String executable = object.getStr("executable");
//         if (executable != null) {
//             this.executable = executable;
//         }
//         String destPath = object.getStr("destPath");
//         if (destPath != null) {
//             this.destPath = destPath;
//         }
//         String mainClass = object.getStr("mainClass");
//         if (mainClass != null) {
//             this.mainClass = mainClass;
//         }
//         String appIcon = object.getStr("appIcon");
//         if (appIcon != null) {
//             this.appIcon = appIcon;
//         }
//         String jarPath = object.getStr("jarPath");
//         if (jarPath != null) {
//             this.jarPath = jarPath;
//         }
//         String jrePath = object.getStr("jrePath");
//         if (jrePath != null) {
//             this.jrePath = jrePath;
//         }
//         String appName = object.getStr("appName");
//         if (appName != null) {
//             this.appName = appName;
//         }
//         String vendor = object.getStr("vendor");
//         if (vendor != null) {
//             this.vendor = vendor;
//         }
//         String desc = object.getStr("desc");
//         if (desc != null) {
//             this.desc = desc;
//         }
//         String platform = object.getStr("platform");
//         if (platform != null) {
//             this.platform = platform;
//         }
//     }
//
//     @Override
//     public PackageConfig clone() {
//         PackageConfig config = new PackageConfig();
//         config.desc = this.desc;
//         config.type = this.type;
//         config.enable = this.enable;
//         config.vendor = this.vendor;
//         config.appName = this.appName;
//         config.version = this.version;
//         config.jarPath = this.jarPath;
//         config.jrePath = this.jrePath;
//         config.appIcon = this.appIcon;
//         config.destPath = this.destPath;
//         config.platform = this.platform;
//         config.buildType = this.buildType;
//         config.mainClass = this.mainClass;
//         config.identifier = this.identifier;
//         config.executable = this.executable;
//         config.compressType = this.compressType;
//         return config;
//     }
//
//     @Override
//     public PackageConfig cross(Object o) {
//         PackageConfig config1 = this.clone();
//         if (o instanceof PackageConfig config) {
//             config1.enable = config.enable;
//             if (config.desc != null) {
//                 config1.desc = config.desc;
//             }
//             if (config.vendor != null) {
//                 config1.vendor = config.vendor;
//             }
//             if (config.appName != null) {
//                 config1.appName = config.appName;
//             }
//             if (config.jarPath != null) {
//                 config1.jarPath = config.jarPath;
//             }
//             if (config.jrePath != null) {
//                 config1.jrePath = config.jrePath;
//             }
//             if (config.appIcon != null) {
//                 config1.appIcon = config.appIcon;
//             }
//             if (config.mainClass != null) {
//                 config1.mainClass = config.mainClass;
//             }
//             if (config.destPath != null) {
//                 config1.destPath = config.destPath;
//             }
//             if (config.executable != null) {
//                 config1.executable = config.executable;
//             }
//             if (config.version != null) {
//                 config1.version = config.version;
//             }
//             if (config.compressType != null) {
//                 config1.compressType = config.compressType;
//             }
//             if (config.type != null) {
//                 config1.type = config.type;
//             }
//             if (config.identifier != null) {
//                 config1.identifier = config.identifier;
//             }
//             if (config.platform != null) {
//                 config1.platform = config.platform;
//             }
//             if (config.buildType != null) {
//                 config1.buildType = config.buildType;
//             }
//         }
//         return config1;
//     }
// }
