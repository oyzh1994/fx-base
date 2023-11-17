// package cn.oyzh.fx.pkg.packager;
//
// import cn.hutool.core.io.FileUtil;
// import com.alibaba.fastjson.JSONObject;
// import lombok.Data;
//
// import java.io.File;
//
// /**
//  * 基础打包参数
//  *
//  * @author oyzh
//  * @since 2023/3/8
//  */
// @Data
// public class BasePkgConfig {
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
//     /**
//      * 解析配置
//      *
//      * @param configPath 配置文件路径
//      */
//     public void parseConfig(String configPath) {
//         String text = FileUtil.readUtf8String(configPath);
//         JSONObject object = JSONObject.parseObject(text);
//         this.fromConfig(object);
//     }
//
//     /**
//      * 从配置对象生成
//      *
//      * @param object 配置对象
//      */
//     protected void fromConfig(JSONObject object) {
//         this.desc = object.getString("desc");
//         this.vendor = object.getString("vendor");
//         this.jrePath = object.getString("jrePath");
//         this.jarPath = object.getString("jarPath");
//         this.appName = object.getString("appName");
//         this.platform = object.getString("platform");
//         this.destPath = object.getString("destPath");
//         this.mainClass = object.getString("mainClass");
//         this.executable = object.getString("executable");
//         this.compressType = object.getString("compressType");
//         if (object.getString("version") != null) {
//             this.version = object.getString("version");
//         }
//         if (object.getString("appIcon") != null) {
//             this.appIcon = object.getString("appIcon");
//         }
//         if (object.getString("type") != null) {
//             this.type = object.getString("type");
//         }
//     }
// }
