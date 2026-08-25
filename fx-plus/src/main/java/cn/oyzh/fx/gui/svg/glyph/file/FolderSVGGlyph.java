package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FolderSVGGlyph extends SVGGlyph {

    public FolderSVGGlyph() {
        super("/fx-svg/file/folder.svg");
    }

    public FolderSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
