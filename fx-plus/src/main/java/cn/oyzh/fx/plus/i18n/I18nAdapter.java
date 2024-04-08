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
     * 获取国际化资源ID
     *
     * @return 国际化资源ID
     */
    default String i18nId() {
        return null;
    }

    /**
     * 获取国际化资源，字符串类型
     *
     * @param key 键
     * @return 国际化资源，字符串类型
     */
    String i18nString(String key);

    /**
     * 更改语言
     *
     * @param locale 语言
     */
    void changeLocale(Locale locale);
}
