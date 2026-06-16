package cn.oyzh.fx.gui.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileAsmSVGGlyph extends SVGGlyph {

    public FileAsmSVGGlyph() {
        super("/fx-svg/file/a/file-asm.svg");
    }

    public FileAsmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
