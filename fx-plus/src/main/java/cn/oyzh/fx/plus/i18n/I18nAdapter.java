package cn.oyzh.fx.plus.i18n;

import cn.oyzh.fx.plus.adapter.PropAdapter;

import java.util.Locale;

/**
 * 国际化适配器
 *
 * @author oyzh
 * @since 2024/4/7
 */
public interface I18nAdapter extends PropAdapter {

    /**
     * 获取国际化资源地址
     *
     * @return 国际化资源地址
     */
    String i18nResource();

    /**
     * 获取国际化资源，字符串类型
     *
     * @param key 键
     * @return 国际化资源，字符串类型
     */
    default String i18nString(String key) {
        return I18nManager.getString(this, key, null);
    }

    /**
     * 禁用国际化
     */
    default void disableI18n() {
        this.setProp("_enableI18n", false);
    }

    /**
     * 启用国际化
     */
    default void enableI18n() {
        this.removeProp("_enableI18n");
    }

    /**
     * 设置启用国际化
     *
     * @param enableI18n 启用国际化
     */
    default void setEnableI18n(boolean enableI18n) {
        this.setProp("_enableI18n", enableI18n);
    }

    /**
     * 是否启用国际化
     *
     * @return 结果
     */
    default boolean isEnableI18n() {
        Boolean b = this.getProp("_enableI18n");
        return b == null || b;
    }

    /**
     * 更改语言
     *
     * @param locale 语言
     */
    void changeLocale(Locale locale);
}
