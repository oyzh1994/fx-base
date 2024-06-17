// package cn.oyzh.fx.pkg.filter;
//
// import cn.hutool.core.io.FileUtil;
// import cn.hutool.core.util.StrUtil;
// import cn.hutool.log.StaticLog;
//
// /**
//  * 文件过滤器
//  *
//  * @author oyzh
//  * @since 2022/12/13
//  */
// //@Slf4j
// public class FileFilter extends BaseFilter {
//
//     @Override
//     public String parseName(String name) {
//         if (StrUtil.isBlank(name)) {
//             return null;
//         }
//         // 替换路径
//         return name.replace("\\", "/").replace("//", "/").toLowerCase();
//     }
//
//     @Override
//     public boolean acceptExclude(String name) {
//         // 排除的文件
//         for (String exclude : this.getExcludes()) {
//
//
//
//             // 文件夹排除
//             if (exclude.endsWith("/") && name.equals(exclude.toLowerCase())) {
//                 StaticLog.warn("{} acceptExclude by exclude:{} equals folder.", name, exclude);
//                 return true;
//             }
//             // 类型排除
//             if (exclude.startsWith(".") && name.endsWith(exclude.toLowerCase())) {
//                 StaticLog.warn("{} acceptExclude by exclude:{} endsWith type.", name, exclude);
//                 return true;
//             }
//             // 包含排除
//             if (name.contains(exclude.toLowerCase())) {
//                 StaticLog.warn("{} acceptExclude by exclude:{} contains.", name, exclude);
//                 return true;
//             }
//         }
//         return false;
//     }
// }
