package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2024-12-17
 */
public class TitleBarFullScreenSVGPane extends HBox implements MouseAdapter, StateAdapter {

    @Getter
    @Setter
    private String size;

    public TitleBarFullScreenSVGPane(String size) {
        this.size = size;
        this.fullScreen();
    }

    public void exit() {
        this.getChildren().setAll(new TitleBarExitFullScreenSVGGlyph(this.size));
    }

    public void fullScreen() {
        this.getChildren().setAll(new TitleBarFullScreenSVGGlyph(this.size));
    }

    public boolean isFullScreen() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("exit-fullscreen.svg");
    }

    public void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            this.fullScreen();
        } else {
            this.exit();
        }
    }
}
