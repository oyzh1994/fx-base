package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * @author oyzh
 * @since 2024/04/09
 */
public class CloseButton extends CancelButton {

    @Override
    public void initNode() {
        this.setText(I18nManager.baseI18nString("btn.stop"));
        super.initNode();
    }
}
