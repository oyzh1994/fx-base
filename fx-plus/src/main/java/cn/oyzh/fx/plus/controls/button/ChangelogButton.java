package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.ChangelogSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

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
