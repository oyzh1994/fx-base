package cn.oyzh.fx.gui.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWebmSVGGlyph extends SVGGlyph {

    public FileWebmSVGGlyph() {
        super("/fx-svg/file/w/file-webm.svg");
    }

    public FileWebmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
