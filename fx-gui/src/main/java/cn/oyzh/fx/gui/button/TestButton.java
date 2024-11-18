package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.fx.gui.svg.glyph.TestSVGGlyph;
import cn.oyzh.i18n.I18nHelper;

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
        this.setText(I18nHelper.test());
        this.setTipText(I18nHelper.test());
        this.init(new TestSVGGlyph(), 0.7);
        super.initNode();
    }
}
