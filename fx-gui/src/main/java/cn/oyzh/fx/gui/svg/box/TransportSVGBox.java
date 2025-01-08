package cn.oyzh.fx.gui.svg.box;

import cn.oyzh.fx.gui.svg.glyph.TransportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGBox;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class TransportSVGBox extends SVGBox {

    public TransportSVGBox() {
        this.setText(I18nHelper.transport());
        this.setGlyph(new TransportSVGGlyph());
    }

    public TransportSVGBox(String size) {
        this();
        this.setSize(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.transport());
        super.initNode();
    }
}
