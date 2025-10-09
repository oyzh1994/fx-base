package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.SplitViewSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class SplitViewButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.init(new SplitViewSVGGlyph(), 0.7);
        super.initNode();
    }
}
