package cn.oyzh.fx.plus.i18n;

import cn.oyzh.fx.plus.stage.StageUtil;
import cn.oyzh.fx.plus.stage.StageWrapper;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 国际化管理器
 *
 * @author oyzh
 * @since 2024/04/07
 */
@UtilityClass
public class I18nManager {

    /**
     * 默认区域
     */
    public static Locale defaultLocale = Locale.PRC;

    /**
     * 当前区域
     */
    private static Locale currentLocale;

    /**
     * 获取当前区域
     *
     * @return 当前区域
     */
    public static Locale currentLocale() {
        if (currentLocale == null) {
            return defaultLocale;
        }
        return currentLocale;
    }

    /**
     * 获取当前区域名称
     *
     * @return 当前区域名称
     */
    public static String currentLocaleName() {
        return Locales.getLocaleName(currentLocale());
    }

    /**
     * 设置区域
     *
     * @param localeName 区域名称
     */
    public static void apply(String localeName) {
        apply(Locales.getLocale(localeName));
    }

    /**
     * 设置区域
     *
     * @param locale 区域
     */
    public static void apply(Locale locale) {
        if (locale == null) {
            locale = defaultLocale;
        }
        try {
            // 变更颜色
            List<StageWrapper> wrappers = StageUtil.allStages();
            for (StageWrapper wrapper : wrappers) {
                if (wrapper.controller() instanceof I18nAdapter adapter) {
                    adapter.changeLocale(locale);
                }
            }
            // 设置当前区域
            currentLocale = locale;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 资源列表
     * key 地区
     * value 资源
     */
    private final Map<Locale, ResourceBundle> resources = new HashMap<>();

    /**
     * 获取基础的国际化资源，字符串
     *
     * @param key 键
     * @return 值
     */
    public static String baseI18nString(String key) {
        ResourceBundle resource = resources.get(currentLocale());
        if (resource == null) {
            resource = ResourceBundle.getBundle("base_i18n", currentLocale());
            resources.put(currentLocale(), resource);
        }
        try {
            if (key.startsWith("base.")) {
                return resource.getString(key);
            }
            return resource.getString("base." + key);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
