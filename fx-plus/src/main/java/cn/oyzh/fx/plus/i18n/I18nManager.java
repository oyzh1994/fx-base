// package cn.oyzh.fx.plus.i18n;
//
// import cn.oyzh.fx.plus.window.StageAdapter;
// import cn.oyzh.fx.plus.window.StageManager;
// import lombok.experimental.UtilityClass;
//
// import java.util.List;
// import java.util.Locale;
//
// /**
//  * 国际化管理器
//  *
//  * @author oyzh
//  * @since 2024/04/07
//  */
// 
// public class I18nManager {
//
//     /**
//      * 默认区域
//      */
//     public static Locale defaultLocale = Locale.PRC;
//
//     /**
//      * 当前区域
//      */
//     private static Locale currentLocale;
//
//     /**
//      * 获取当前区域
//      *
//      * @return 当前区域
//      */
//     public static Locale currentLocale() {
//         if (currentLocale == null) {
//             return defaultLocale;
//         }
//         return currentLocale;
//     }
//
//     /**
//      * 获取当前区域名称
//      *
//      * @return 当前区域名称
//      */
//     public static String currentLocaleName() {
//         return Locales.getLocaleName(currentLocale());
//     }
//
//     /**
//      * 设置区域
//      *
//      * @param localeName 区域名称
//      */
//     public static void apply(String localeName) {
//         apply(Locales.getLocale(localeName));
//     }
//
//     /**
//      * 设置区域
//      *
//      * @param locale 区域
//      */
//     public static void apply(Locale locale) {
//         if (locale == null) {
//             locale = defaultLocale;
//         }
//         try {
//             // 变更颜色
//             List<StageAdapter> wrappers = StageManager.allStages();
//             for (StageAdapter wrapper : wrappers) {
//                 if (wrapper.controller() instanceof I18nAdapter adapter) {
//                     adapter.changeLocale(locale);
//                 }
//             }
//             // 设置当前区域
//             currentLocale = locale;
//             // 设置系统区域
//             Locale.setDefault(currentLocale);
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//     }
//
//
// }
