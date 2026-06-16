package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FolderAddSVGGlyph extends SVGGlyph {

    public FolderAddSVGGlyph() {
        super("/fx-svg/file/folder-add.svg");
    }

    public FolderAddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
