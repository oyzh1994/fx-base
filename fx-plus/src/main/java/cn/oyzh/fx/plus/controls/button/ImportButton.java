package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ImportButton extends IconButton{

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(BaseResourceBundle.getBaseString("import"));
        this.setTipText(BaseResourceBundle.getBaseString("import"));
        this.init("/fx-plus/font/Import.svg", 0.7);
        super.initNode();
    }
}
