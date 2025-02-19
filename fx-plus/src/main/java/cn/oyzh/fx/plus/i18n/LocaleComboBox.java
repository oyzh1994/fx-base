package cn.oyzh.fx.plus.i18n;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.fx.plus.controls.combo.FlexComboBox;
import cn.oyzh.i18n.I18nLocales;
import cn.oyzh.i18n.I18nManager;

import java.util.Locale;

/**
 * 区域下拉框
 *
 * @author oyzh
 * @since 2024/04/07
 */
public class LocaleComboBox extends FlexComboBox<Locale> {

    {
        this.addItems(I18nLocales.locales());
        this.setTipText(I18nResourceBundle.i18nString("base.localeTip"));
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(Locale o) {
                return I18nLocales.getLocaleDesc(o);
            }
        });
    }

    /**
     * 选择区域
     *
     * @param localeName 区域名称
     */
    public void select(String localeName) {
        if (StringUtil.isEmpty(localeName)) {
            this.select(I18nManager.defaultLocale);
        } else {
            try {
                super.select(I18nLocales.getLocale(localeName));
            } catch (Exception ex) {
                ex.printStackTrace();
                this.select(0);
            }
        }
    }

    /**
     * 获取区域名称
     *
     * @return 区域名称
     */
    public String name() {
        return I18nLocales.getLocaleName(this.getValue());
    }
}
