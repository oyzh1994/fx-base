package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.TestSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * 测试按钮
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class TestButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("success");
        this.setText(I18nResourceBundle.i18nString("base.test"));
        this.setTipText(I18nResourceBundle.i18nString("base.test"));
        this.init(new TestSVGGlyph(), 0.7);
        super.initNode();
    }
}
