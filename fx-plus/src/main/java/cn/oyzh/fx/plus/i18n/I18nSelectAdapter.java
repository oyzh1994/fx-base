package cn.oyzh.fx.plus.i18n;

import java.util.List;
import java.util.Locale;

/**
 * 国际化下拉项适配器，按地区提供可选项列表
 *
 * @author oyzh
 * @since 2024/4/11
 */
public interface I18nSelectAdapter<V> {

    /**
     * 获取指定地区下的值列表
     *
     * @param locale 地区
     * @return 值列表
     */
    List<V> values(Locale locale);

    /**
     * 获取默认地区下的值列表
     *
     * @return 值列表
     */
    default List<V> default_Values() {
        return this.values(Locale.getDefault());
    }

    /**
     * 获取简体中文地区下的值列表
     *
     * @return 值列表
     */
    default List<V> zh_CN_Values() {
        return this.values(Locale.PRC);
    }

    /**
     * 获取繁体中文地区下的值列表
     *
     * @return 值列表
     */
    default List<V> zh_TW_Values() {
        return this.values(Locale.TAIWAN);
    }

    /**
     * 获取英文地区下的值列表
     *
     * @return 值列表
     */
    default List<V> english_Values() {
        return this.values(Locale.ENGLISH);
    }
}
