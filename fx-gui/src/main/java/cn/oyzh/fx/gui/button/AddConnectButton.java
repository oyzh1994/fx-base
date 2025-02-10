package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.AddSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/04/08
 */
public class AddConnectButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.addClass("success");
        this.setText(I18nHelper.addConnect());
        this.setTipText(I18nHelper.addConnect());
        this.init(new AddSVGGlyph(), 0.7);
        super.initNode();
    }

}
