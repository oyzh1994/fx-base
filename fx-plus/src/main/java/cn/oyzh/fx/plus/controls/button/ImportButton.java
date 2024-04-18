package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ImportButton extends IconButton{

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nResourceBundle.i18nString("base.import"));
        this.setTipText(I18nResourceBundle.i18nString("base.import"));
        this.init("/fx-plus/font/Import.svg", 0.7);
        super.initNode();
    }
}
