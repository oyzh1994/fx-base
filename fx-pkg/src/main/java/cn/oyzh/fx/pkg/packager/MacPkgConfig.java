// package cn.oyzh.fx.pkg.packager;
//
// import com.alibaba.fastjson.JSONObject;
// import lombok.Data;
// import lombok.EqualsAndHashCode;
//
// /**
//  * mac打包配置
//  *
//  * @author oyzh
//  * @since 2023/3/8
//  */
// @Data
// @EqualsAndHashCode(callSuper = true)
// public class MacPkgConfig extends BasePkgConfig {
//
//     /**
//      * 程序唯一标识符
//      */
//     private String identifier;
//
//     @Override
//     protected void fromConfig(JSONObject object) {
//         super.fromConfig(object);
//         this.identifier = object.getString("identifier");
//     }
//
//     /**
//      * 从配置文件生成配置对象
//      *
//      * @param configPath 配置文件路径
//      * @return 配置对象
//      */
//     public static MacPkgConfig fromConfig(String configPath) {
//         MacPkgConfig config = new MacPkgConfig();
//         config.parseConfig(configPath);
//         return config;
//     }
// }
