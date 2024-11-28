package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.OldSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;

/**
 * @author oyzh
 * @since 2024/08/26
 */
public class OldButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("danger");
        this.init(new OldSVGGlyph(), 0.7);
        super.initNode();
    }
}
