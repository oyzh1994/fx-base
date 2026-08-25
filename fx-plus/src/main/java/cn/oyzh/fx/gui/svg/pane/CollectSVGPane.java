package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.fx.gui.svg.glyph.CollectSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnCollectSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPane;
import javafx.scene.text.Font;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class CollectSVGPane extends SVGPane {

    public CollectSVGPane() {
        this.unCollect();
    }

    public void collect() {
        this.setChild(new UnCollectSVGGlyph(this.getSize()));
    }

    public void unCollect() {
        this.setChild(new CollectSVGGlyph(this.getSize()));
    }

    public boolean isCollect() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("star.svg");
    }

    public void setCollect(boolean collect) {
        if (collect) {
            this.collect();
        } else {
            this.unCollect();
        }
    }

    @Override
    public void changeFont(Font font) {
        if (!this.isChildEmpty() && this.isEnableFont()) {
            if (this.isCollect()) {
                this.collect();
            } else {
                this.unCollect();
            }
        }
        super.changeFont(font);
    }
}
