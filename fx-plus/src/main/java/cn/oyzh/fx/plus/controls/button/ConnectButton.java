package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;

/**
 * 分组按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class ConnectButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("success");
        this.setText(I18nManager.baseI18nString("btn.connect"));
        this.init("/fx-plus/font/add.svg", 0.7);
        super.initNode();
    }

}
