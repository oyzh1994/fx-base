package cn.oyzh.fx.plus.i18n;

import cn.hutool.core.util.StrUtil;

import javax.annotation.Nonnull;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ResourceBundleWrapper extends ResourceBundle {

    private final ResourceBundle resources;

    public ResourceBundleWrapper(ResourceBundle resources) {
        this.resources = resources;
    }

    @Override
    protected Object handleGetObject(@Nonnull String key) {
        Object val = null;
        if (key.startsWith("base.")) {
            val = BaseResourceBundle.getBaseObject(key);
        }
        if (val == null) {
            try {
                val = this.resources.getObject(key);
            } catch (MissingResourceException ex) {
                ex.printStackTrace();
            }
        }
        return val;
    }

    @Nonnull
    @Override
    public Enumeration<String> getKeys() {
        return this.resources.getKeys();
    }

    public boolean containsKey(@Nonnull String key) {
        return super.containsKey(key) || BaseResourceBundle.containsBaseKey(key);
    }

    public static ResourceBundleWrapper newResource(String resource) {
        if (StrUtil.isNotBlank(resource)) {
            ResourceBundle bundle = ResourceBundle.getBundle(resource, I18nManager.currentLocale());
            return new ResourceBundleWrapper(bundle);
        }
        return null;
    }
}
