package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.UnLockSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;

/**
 * 取消按钮
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class UnLockButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.addClass("accent");
        this.init(new UnLockSVGGlyph(), 0.7);
        super.initNode();
    }
}
