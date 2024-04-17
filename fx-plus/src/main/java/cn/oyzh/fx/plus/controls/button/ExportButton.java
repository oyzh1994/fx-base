package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ExportButton extends IconButton{

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(BaseResourceBundle.getBaseString("base.export"));
        this.setTipText(BaseResourceBundle.getBaseString("base.export"));
        this.init("/fx-plus/font/export.svg", 0.7);
        super.initNode();
    }
}
