package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.fx.gui.svg.glyph.HiddenSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ShowSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPane;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class HiddenSVGPane extends SVGPane {

    public HiddenSVGPane() {
        this.hidden();
    }

    public void show() {
        this.setChild(new ShowSVGGlyph(this.size));
    }

    public void hidden() {
        this.setChild(new HiddenSVGGlyph(this.size));
    }

    public boolean isHidden() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("hidden.svg");
    }

    public void setHidden(boolean hidden) {
        if (hidden) {
            this.hidden();
        } else {
            this.show();
        }
    }
}
