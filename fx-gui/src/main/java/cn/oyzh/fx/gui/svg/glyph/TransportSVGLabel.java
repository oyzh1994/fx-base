package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class TransportSVGLabel extends SVGLabel {

    public TransportSVGLabel() {
        this.setGraphic(new TransportSVGGlyph());
    }

    public TransportSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.transport());
        this.setTipText(I18nHelper.transport());
        super.initNode();
    }
}
