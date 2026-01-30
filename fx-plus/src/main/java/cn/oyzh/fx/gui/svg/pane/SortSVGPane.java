package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.fx.gui.svg.glyph.SortAscSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SortDescSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPane;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class SortSVGPane extends SVGPane {

    public SortSVGPane() {
        this.desc();
    }

    public void desc() {
        this.setChild(new SortAscSVGGlyph(this.size));
    }

    public void asc() {
        this.setChild(new SortDescSVGGlyph(this.size));
    }

    public boolean isAsc() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("sort-descending.svg");
    }

    public void setAsc(boolean asc) {
        if (asc) {
            this.asc();
        } else {
            this.desc();
        }
    }
}
