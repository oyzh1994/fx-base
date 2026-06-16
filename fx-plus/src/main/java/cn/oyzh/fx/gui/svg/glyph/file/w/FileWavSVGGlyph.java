package cn.oyzh.fx.gui.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWavSVGGlyph extends SVGGlyph {

    public FileWavSVGGlyph() {
        super("/fx-svg/file/w/file-wav.svg");
    }

    public FileWavSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
