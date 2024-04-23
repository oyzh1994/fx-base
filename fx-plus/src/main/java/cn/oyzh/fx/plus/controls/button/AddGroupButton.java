package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.AddSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

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
        this.setText(I18nResourceBundle.i18nString("base.addGroup"));
        this.setTipText(I18nResourceBundle.i18nString("base.addGroup"));
        this.init(new AddSVGGlyph(), 0.7);
        super.initNode();
    }
}
