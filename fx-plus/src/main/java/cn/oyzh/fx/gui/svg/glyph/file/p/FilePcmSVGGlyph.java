package cn.oyzh.fx.gui.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePcmSVGGlyph extends SVGGlyph {

    public FilePcmSVGGlyph() {
        super("/fx-svg/file/p/file-pcm.svg");
    }

    public FilePcmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
