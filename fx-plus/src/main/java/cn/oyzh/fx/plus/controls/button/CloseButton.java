package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/04/09
 */
public class CloseButton extends CancelButton {

    @Override
    public void initNode() {
        super.initNode();
        this.setText(I18nHelper.close());
        this.setTipText(I18nHelper.close());
    }
}
