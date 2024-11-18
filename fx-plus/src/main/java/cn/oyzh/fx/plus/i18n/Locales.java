// package cn.oyzh.fx.plus.i18n;
//
// import cn.oyzh.common.util.StringUtil;
// import lombok.experimental.UtilityClass;
//
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Locale;
//
// /**
//  * @author oyzh
//  * @since 2024/4/7
//  */
// @UtilityClass
// public class Locales {
//
//     /**
//      * 获取区域列表
//      *
//      * @return 区域列表
//      */
//     public static List<Locale> locales() {
//         List<Locale> locales = new ArrayList<>();
//         locales.add(Locale.PRC);
//         locales.add(Locale.TAIWAN);
//         locales.add(Locale.ENGLISH);
//         return locales;
//     }
//
//     /**
//      * 获取区域描述
//      *
//      * @param locale 区域
//      * @return 区域描述
//      */
//     public static String getLocaleDesc(Locale locale) {
//         if (locale == Locale.PRC) {
//             return "中文简体";
//         }
//         if (locale == Locale.TAIWAN) {
//             return "中文繁體";
//         }
//         if (locale == Locale.ENGLISH) {
//             return "English";
//         }
//         return "中文简体";
//     }
//
//     /**
//      * 获取区域名称
//      *
//      * @param locale 区域
//      * @return 区域名称
//      */
//     public static String getLocaleName(Locale locale) {
//         if (locale == Locale.PRC) {
//             return "zh_cn";
//         }
//         if (locale == Locale.TAIWAN) {
//             return "zh_tw";
//         }
//         if (locale == Locale.ENGLISH) {
//             return "en";
//         }
//         return "zh_cn";
//     }
//
//     /**
//      * 获取区域
//      *
//      * @param localeName 区域名称
//      * @return 区域
//      */
//     public static Locale getLocale(String localeName) {
//         if (StringUtil.equals(localeName, "zh_cn")) {
//             return Locale.PRC;
//         }
//         if (StringUtil.equals(localeName, "zh_tw")) {
//             return Locale.TAIWAN;
//         }
//         if (StringUtil.equals(localeName, "en")) {
//             return Locale.ENGLISH;
//         }
//         return Locale.PRC;
//     }
// }
