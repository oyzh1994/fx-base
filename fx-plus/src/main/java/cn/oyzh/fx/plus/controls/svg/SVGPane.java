package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.mouse.MouseAdapter;

/**
 * svg面板
 *
 * @author oyzh
 * @since 2025/01/07
 */
public class SVGPane extends FXHBox implements MouseAdapter {

    protected String size;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
        SVGGlyph glyph = (SVGGlyph) this.getChild(0);
        if (glyph != null) {
            glyph.setSizeStr(size);
        }
    }
}
