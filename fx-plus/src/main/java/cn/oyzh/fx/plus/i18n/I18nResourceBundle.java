package cn.oyzh.fx.plus.i18n;

import cn.hutool.core.util.StrUtil;

import javax.annotation.Nonnull;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class I18nResourceBundle extends ResourceBundle {

    /**
     * 当前实例
     */
    public static final I18nResourceBundle INSTANCE = new I18nResourceBundle();

    /**
     * 基础资源列表
     * key 地区
     * value 资源
     */
    private final Map<Locale, ResourceBundle> base_resources = new HashMap<>();

    /**
     * 项目资源列表
     * key 地区
     * value 资源
     */
    private final Map<Locale, ResourceBundle> i18n_resources = new HashMap<>();

    private I18nResourceBundle() {

    }

    @Override
    protected Object handleGetObject(@Nonnull String key) {
        try {
            ResourceBundle resource = this.initResource(key);
            if (resource != null) {
                return resource.getObject(key);
            }
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Nonnull
    @Override
    public Enumeration<String> getKeys() {
        return new Enumeration<>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public String nextElement() {
                return null;
            }
        };
    }

    /**
     * 初始化资源
     *
     * @param key 键
     * @return 资源
     */
    private ResourceBundle initResource(String key) {
        ResourceBundle resource;
        if (key.startsWith("base.")) {
            resource = this.base_resources.get(I18nManager.currentLocale());
            if (resource == null) {
                resource = ResourceBundle.getBundle("base_i18n", I18nManager.currentLocale());
                this.base_resources.put(I18nManager.currentLocale(), resource);
            }
        } else {
            resource = this.i18n_resources.get(I18nManager.currentLocale());
            if (resource == null) {
                resource = ResourceBundle.getBundle("i18n", I18nManager.currentLocale());
                this.i18n_resources.put(I18nManager.currentLocale(), resource);
            }
        }
        return resource;
    }

    @Override
    public boolean containsKey(@Nonnull String key) {
        ResourceBundle resource = this.initResource(key);
        return resource.containsKey(key);
    }

    /**
     * 获取基础的国际化资源，字符串
     *
     * @param key 键
     * @return 值
     */
    public static String i18nString(String key) {
        try {
            return INSTANCE.getString(key);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 获取基础的国际化资源，对象
     *
     * @param keys 键
     * @return 值
     */
    public static String i18nString(String... keys) {
        try {
            StringBuilder builder = new StringBuilder();
            for (String key : keys) {
                String val = INSTANCE.getString(key);
                builder.append(val);
                if (I18nManager.currentLocale() == Locale.ENGLISH) {
                    builder.append(" ").append(StrUtil.lowerFirst(val));
                } else {
                    builder.append(val);
                }
            }
            if (I18nManager.currentLocale() == Locale.ENGLISH) {
                return builder.substring(1, builder.length());
            }
            return builder.toString();
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
    public static Object i18nObject(String key) {
        try {
            return INSTANCE.getObject(key);
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
    public static boolean containsI18nKey(String key) {
        return INSTANCE.containsKey(key);
    }
}
