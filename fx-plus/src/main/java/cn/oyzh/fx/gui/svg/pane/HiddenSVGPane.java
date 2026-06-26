package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.svg.glyph.EyeCloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.EyeOpenSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPane;
import cn.oyzh.fx.plus.font.FontManager;
import javafx.geometry.Insets;
import javafx.scene.text.Font;

/**
 * @author oyzh
 * @since 2024-12-09
 */
public class HiddenSVGPane extends SVGPane {

    public HiddenSVGPane() {
        this.hidden();
    }

    public void show() {
        this.setChild(new EyeOpenSVGGlyph(this.getSize()));
    }

    public void hidden() {
        this.setChild(new EyeCloseSVGGlyph(this.getSize()));
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

    @Override
    public void changeFont(Font font) {
        if (!this.isChildEmpty() && this.isEnableFont()) {
            if (this.isHidden()) {
                this.hidden();
            } else {
                this.show();
            }
        }
        super.changeFont(font);
    }
}
