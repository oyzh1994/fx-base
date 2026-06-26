package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.fx.gui.svg.glyph.layout.Layout1SVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.layout.Layout2SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPane;
import javafx.scene.text.Font;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class LayoutSVGPane extends SVGPane {

    public LayoutSVGPane() {
        this.layout2();
    }

    public void layout1() {
        this.setChild(new Layout1SVGGlyph(this.getSize()));
    }

    public void layout2() {
        this.setChild(new Layout2SVGGlyph(this.getSize()));
    }

    public boolean isLayout1() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("layout1.svg");
    }

    @Override
    public void changeFont(Font font) {
        if (!this.isChildEmpty() && this.isEnableFont()) {
            if (this.isLayout1()) {
                this.layout1();
            } else {
                this.layout2();
            }
        }
        super.changeFont(font);
    }
}
