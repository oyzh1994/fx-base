package cn.oyzh.fx.gui.svg.glyph.file.f;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileFlacSVGGlyph extends SVGGlyph {

    public FileFlacSVGGlyph() {
        super("/fx-svg/file/f/file-flac.svg");
    }

    public FileFlacSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
