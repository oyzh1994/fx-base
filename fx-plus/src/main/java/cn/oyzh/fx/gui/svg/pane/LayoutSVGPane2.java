package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.fx.gui.svg.glyph.layout.Layout1SVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.layout.Layout2SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.svg.SVGPane;
import javafx.scene.text.Font;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class LayoutSVGPane2 extends SVGPane {

    public LayoutSVGPane2() {
        this.layout1();
    }

    public void layout1() {
        this.setChild(new Layout1SVGGlyph(this.getSize()));
    }

    public void layout2() {
        this.setChild(new Layout2SVGGlyph(this.getSize()));
    }

    public boolean isLayout1() {
        SVGLabel label = (SVGLabel) this.getChildren().getFirst();
        SVGGlyph svgGlyph = label.graphic();
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
