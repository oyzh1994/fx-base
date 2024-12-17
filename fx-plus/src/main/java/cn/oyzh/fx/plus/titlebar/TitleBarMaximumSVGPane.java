package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class TitleBarMaximumSVGPane extends HBox implements MouseAdapter, StateAdapter {

    @Getter
    @Setter
    private String size;

    public TitleBarMaximumSVGPane(String size) {
        this.size = size;
        this.maximize();
    }

    public void restore() {
        this.getChildren().setAll(new TitleBarUnMaximumSVGGlyph(this.size));
    }

    public void maximize() {
        this.getChildren().setAll(new TitleBarMaximumSVGGlyph(this.size));
    }

    public boolean isMaximize() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("un-maximum.svg");
    }

    public void setMaximize(boolean maximize) {
        if (maximize) {
            this.maximize();
        } else {
            this.restore();
        }
    }
}
