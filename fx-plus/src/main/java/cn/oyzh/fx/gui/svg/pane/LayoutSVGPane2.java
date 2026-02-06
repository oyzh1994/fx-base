package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.fx.gui.svg.label.Layout1SVGLabel;
import cn.oyzh.fx.gui.svg.label.Layout2SVGLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.svg.SVGPane;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class LayoutSVGPane2 extends SVGPane {

    public LayoutSVGPane2() {
        this.layout1();
    }

    public void layout1() {
        this.setChild(new Layout1SVGLabel(this.size));
    }

    public void layout2() {
        this.setChild(new Layout2SVGLabel(this.size));
    }

    public boolean isLayout1() {
        SVGLabel label = (SVGLabel) this.getChildren().getFirst();
        SVGGlyph svgGlyph = label.graphic();
        return svgGlyph.getUrl().contains("layout1.svg");
    }
}
