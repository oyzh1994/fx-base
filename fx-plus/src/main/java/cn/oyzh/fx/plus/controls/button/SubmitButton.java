package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.SubmitSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * 取消按钮
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class SubmitButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(I18nHelper.submit());
        this.setTipText(I18nHelper.submit());
        this.init(new SubmitSVGGlyph(), 0.7);
        super.initNode();
    }
}
