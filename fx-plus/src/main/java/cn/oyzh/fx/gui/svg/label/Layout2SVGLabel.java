package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.layout.Layout2SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class Layout2SVGLabel extends SVGLabel {

    public Layout2SVGLabel() {
        this.setGraphic(new Layout2SVGGlyph());
    }

    public Layout2SVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.layout() + "2");
        super.initNode();
    }
}
