package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.CopySVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * 复制按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class CopyButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nResourceBundle.i18nString("base.copy"));
        this.setTipText(I18nResourceBundle.i18nString("base.copy"));
        this.init(new CopySVGGlyph(), 0.7);
        super.initNode();
    }
}
