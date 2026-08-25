package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.BoxSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class BoxSVGLabel extends SVGLabel {

    public BoxSVGLabel() {
        this.setGraphic(new BoxSVGGlyph());
    }

    public BoxSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

}
