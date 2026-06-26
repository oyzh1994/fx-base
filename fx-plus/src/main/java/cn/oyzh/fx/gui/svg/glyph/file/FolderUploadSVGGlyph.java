package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FolderUploadSVGGlyph extends SVGGlyph {

    public FolderUploadSVGGlyph() {
        super("/fx-svg/file/folder-upload.svg");
    }

    public FolderUploadSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
