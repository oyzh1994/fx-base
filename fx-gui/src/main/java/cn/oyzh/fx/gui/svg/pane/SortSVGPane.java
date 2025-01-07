package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.fx.gui.svg.glyph.SortAscSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SortDescSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPane;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class SortSVGPane extends SVGPane   {

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
