package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/04/09
 */
public class CloseButton extends CancelButton {

    @Override
    public void initNode() {
        this.setText(I18nResourceBundle.i18nString("base.close"));
        this.setTipText(I18nResourceBundle.i18nString("base.close"));
        super.initNode();
    }
}
