package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.AddSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 分组按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class AddGroupButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(I18nHelper.addGroup());
        this.setTipText(I18nHelper.addGroup());
        this.init(new AddSVGGlyph(), 0.7);
        super.initNode();
    }
}
