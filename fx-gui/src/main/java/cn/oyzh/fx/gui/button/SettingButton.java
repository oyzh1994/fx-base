package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 设置按钮
 *
 * @author oyzh
 * @since 2024/07/11
 */
public class SettingButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper.setting());
        this.setTipText(I18nHelper.setting());
        this.init(new SettingSVGGlyph(), 0.7);
        super.initNode();
    }
}
