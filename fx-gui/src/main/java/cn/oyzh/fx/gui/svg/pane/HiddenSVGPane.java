package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.fx.gui.svg.glyph.EyeCloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.EyeOpenSVGGlyph;
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
        this.setChild(new EyeOpenSVGGlyph(this.size));
    }

    public void hidden() {
        this.setChild(new EyeCloseSVGGlyph(this.size));
    }

    public boolean isHidden() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("eye-close.svg");
    }

    public void setHidden(boolean hidden) {
        if (hidden) {
            this.hidden();
        } else {
            this.show();
        }
    }
}
