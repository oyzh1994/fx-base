package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.ChangelogSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 更新日志按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class ChangelogButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("danger");
        this.setText(I18nHelper.changelog());
        this.setTipText(I18nHelper.changelog());
        this.init(new ChangelogSVGGlyph(), 0.7);
        super.initNode();
    }
}
