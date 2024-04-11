package cn.oyzh.fx.plus.i18n;

import java.util.List;
import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public interface I18nSelectAdapter<V> {

    List<V> values(Locale locale);

    default List<V> default_Values() {
        return this.values(Locale.getDefault());
    }

    default List<V> zh_CN_Values() {
        return this.values(Locale.PRC);
    }

    default List<V> zh_TW_Values() {
        return this.values(Locale.TAIWAN);
    }

    default List<V> english_Values() {
        return this.values(Locale.ENGLISH);
    }
}
