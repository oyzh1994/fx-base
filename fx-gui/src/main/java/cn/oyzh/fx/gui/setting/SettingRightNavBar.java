package cn.oyzh.fx.gui.setting;

import cn.oyzh.fx.plus.controls.label.FlexLabel;

/**
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingRightNavBar extends FlexLabel {

    {
        this.disableFont();
        this.setFontSize(13);
        this.setPrefHeight(30);
        this.setFontWeight2(700);
        this.setId("right-nav-bar");
        this.addClass("accent");
    }
}
