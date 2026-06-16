package cn.oyzh.fx.gui.svg.glyph.file.g;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileGzSVGGlyph extends SVGGlyph {

    public FileGzSVGGlyph() {
        super("/fx-svg/file/g/file-gzip.svg");
    }

    public FileGzSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
