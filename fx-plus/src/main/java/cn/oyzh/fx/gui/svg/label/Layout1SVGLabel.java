package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.layout.Layout1SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class Layout1SVGLabel extends SVGLabel {

    public Layout1SVGLabel() {
        this.setGraphic(new Layout1SVGGlyph());
    }

    public Layout1SVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.layout() + "1");
        super.initNode();
    }
}
