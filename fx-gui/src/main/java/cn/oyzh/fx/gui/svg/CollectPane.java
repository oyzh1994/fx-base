package cn.oyzh.fx.gui.svg;

import cn.oyzh.fx.gui.svg.glyph.CollectSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnCollectSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class CollectPane extends HBox implements MouseAdapter {

    @Getter
    @Setter
    private String size;

    public CollectPane() {
        this.unCollect();
    }

    public void collect() {
        this.getChildren().setAll(new UnCollectSVGGlyph(this.size));
    }

    public void unCollect() {
        this.getChildren().setAll(new CollectSVGGlyph(this.size));
    }

    public boolean isCollect() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("star.svg");
    }

    public void setCollect(boolean collect) {
        if (collect) {
            this.unCollect();
        } else {
            this.collect();
        }
    }
}
