package cn.oyzh.fx.gui.svg.glyph.file.u;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileUploadSVGGlyph extends SVGGlyph {

    public FileUploadSVGGlyph() {
        super("/fx-svg/file/u/file-upload.svg");
    }

    public FileUploadSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
