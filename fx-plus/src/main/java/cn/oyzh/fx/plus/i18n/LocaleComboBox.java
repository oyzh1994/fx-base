package cn.oyzh.fx.plus.i18n;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.SimpleStringConverter;
import cn.oyzh.fx.plus.controls.combo.FlexComboBox;

import java.util.Locale;

/**
 * 区域下拉框
 *
 * @author oyzh
 * @since 2024/04/07
 */
public class LocaleComboBox extends FlexComboBox<Locale> {

    {
        this.addItems(Locales.locales());
        this.setTipText(BaseResourceBundle.getBaseString("base.localeTip"));
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(Locale o) {
                return Locales.getLocaleDesc(o);
            }
        });
    }

    /**
     * 选择区域
     *
     * @param localeName 区域名称
     */
    public void select(String localeName) {
        if (StrUtil.isEmpty(localeName)) {
            this.select(I18nManager.defaultLocale);
        } else {
            try {
                super.select(Locales.getLocale(localeName));
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
        return Locales.getLocaleName(this.getValue());
    }
}
