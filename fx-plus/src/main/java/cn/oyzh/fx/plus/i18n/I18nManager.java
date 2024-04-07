package cn.oyzh.fx.plus.i18n;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.WeakCache;
import cn.oyzh.fx.plus.stage.StageUtil;
import cn.oyzh.fx.plus.stage.StageWrapper;
import cn.oyzh.fx.plus.util.ResourceUtil;
import lombok.experimental.UtilityClass;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * 国际化管理器
 *
 * @author oyzh
 * @since 2024/04/07
 */
@UtilityClass
public class I18nManager {

    /**
     * 资源列表
     */
    // public static Map<WeakReference<I18nAdapter>, Map<String, String>> resources = new HashMap<>();
    public static WeakCache<I18nAdapter, Map<String, String>> resources = CacheUtil.newWeakCache(5 * 60 * 1000L);

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
     * 加载资源
     *
     * @param adapter 国际化适配器
     */
    public static void loadResource(I18nAdapter adapter) {
        try {
            if (adapter.i18nResource() != null) {
                URL url1 = ResourceUtil.getResource(adapter.i18nResource());
                Properties properties = new Properties();
                properties.load(url1.openStream());
                Map<String, String> resource = resources.get(adapter);
                if (resource == null) {
                    resource = new HashMap<>();
                    resources.put(adapter, resource);
                }
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    resource.put((String) entry.getKey(), (String) entry.getValue());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 取消加载资源
     *
     * @param adapter 国际化适配器
     */
    public static void unloadResource(I18nAdapter adapter) {
        resources.remove(adapter);
    }

    /**
     * 获取字符串
     *
     * @param adapter  国际化适配器
     * @param key      键
     * @param defValue 默认值
     * @return 字符串
     */
    public static String getString(I18nAdapter adapter, String key, String defValue) {
        Map<String, String> resource = resources.get(adapter);
        if (resource == null) {
            return defValue;
        }
        Object val = resource.get(key + "." + I18nManager.currentLocaleName());
        return val == null ? defValue : val.toString();
    }
}
