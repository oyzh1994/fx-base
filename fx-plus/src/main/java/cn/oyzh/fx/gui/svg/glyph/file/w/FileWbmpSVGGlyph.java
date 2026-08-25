package cn.oyzh.fx.gui.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWbmpSVGGlyph extends SVGGlyph {

    public FileWbmpSVGGlyph() {
        super("/fx-svg/file/w/file-wbmp.svg");
    }

    public FileWbmpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
