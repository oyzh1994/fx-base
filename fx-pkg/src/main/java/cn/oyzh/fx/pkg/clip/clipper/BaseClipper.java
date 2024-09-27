// package cn.oyzh.fx.pkg.clip.clipper;
//
// import cn.hutool.log.StaticLog;
// import cn.oyzh.fx.pkg.filter.FileFilter;
// import lombok.Getter;
// import lombok.experimental.Accessors;
//
// import java.util.Set;
//
// /**
//  * jar裁剪工具
//  *
//  * @author oyzh
//  * @since 2022/12/7
//  */
// //@Slf4j
// public abstract class BaseClipper {
//
//     /**
//      * 文件过滤器
//      */
//     @Getter
//     @Accessors(chain = true, fluent = true)
//     protected final FileFilter fileFilter = new FileFilter();
//
//     /**
//      * 裁剪
//      *
//      * @param src  源文件
//      * @param dest 目标文件
//      */
//     public void clip(String src, String dest) throws Exception{
//         this.checkPath(src, dest);
//     }
//
//     /**
//      * 执行过滤
//      *
//      * @param name 名称
//      * @return 结果
//      */
//     public boolean filterName(String name) {
//         boolean accept = this.fileFilter.accept(name);
//         if (!accept) {
//             JulLog.info("file:{} filtered.", name);
//         }
//         return accept;
//     }
//
//     /**
//      * 添加排除的文件名称
//      *
//      * @param excludeFiles 排除的文件名称列表
//      */
//     public void addExcludeFiles(Set<String> excludeFiles) {
//         this.fileFilter.addExcludes(excludeFiles);
//     }
//
//     /**
//      * 检查路径
//      *
//      * @param src  源路径
//      * @param dest 目标路径
//      */
//     protected void checkPath(String src, String dest) {
//     }
//
// }
