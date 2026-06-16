package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTerminalSVGGlyph extends SVGGlyph {

    public FileTerminalSVGGlyph() {
        super("/fx-svg/file/t/file-terminal.svg");
    }

    public FileTerminalSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
