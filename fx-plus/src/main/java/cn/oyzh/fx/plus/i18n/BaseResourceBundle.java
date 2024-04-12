package cn.oyzh.fx.plus.i18n;

import javax.annotation.Nonnull;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class BaseResourceBundle extends ResourceBundle {

    private static final BaseResourceBundle BASE_RESOURCE = new BaseResourceBundle();


    /**
     * 资源列表
     * key 地区
     * value 资源
     */
    private final Map<Locale, ResourceBundle> resources = new HashMap<>();


    private BaseResourceBundle() {

    }

    @Override
    protected Object handleGetObject(@Nonnull String key) {
        try {
            ResourceBundle resource = this.initResource();
            if (key.startsWith("base.")) {
                return resource.getObject(key);
            }
            return resource.getObject("base." + key);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private ResourceBundle initResource() {
        ResourceBundle resource = this.resources.get(I18nManager.currentLocale());
        if (resource == null) {
            resource = ResourceBundle.getBundle("base_i18n", I18nManager.currentLocale());
            this.resources.put(I18nManager.currentLocale(), resource);
        }
        return resource;
    }

    @Nonnull
    @Override
    public Enumeration<String> getKeys() {
        ResourceBundle resource = this.initResource();
        return resource.getKeys();
    }

    @Override
    public boolean containsKey(@Nonnull String key) {
        ResourceBundle resource = this.initResource();
        return resource.containsKey(key);
    }

    /**
     * 获取基础的国际化资源，字符串
     *
     * @param key 键
     * @return 值
     */
    public static String getBaseString(String key) {
        try {
            return BASE_RESOURCE.getString(key);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 获取基础的国际化资源，对象
     *
     * @param key 键
     * @return 值
     */
    public static Object getBaseObject(String key) {
        try {
            return BASE_RESOURCE.getString(key);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 是否存在值
     *
     * @param key 键
     * @return 值
     */
    public static boolean containsBaseKey(String key) {
        return BASE_RESOURCE.containsKey(key);
    }

}
