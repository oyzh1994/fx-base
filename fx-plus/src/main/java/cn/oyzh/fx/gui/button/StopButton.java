package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.StopSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 取消按钮
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class StopButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.addClass("default");
        this.setText(I18nHelper.stop());
        this.setTipText(I18nHelper.stop());
        this.init(new StopSVGGlyph(), 0.7);
        super.initNode();
    }
}
