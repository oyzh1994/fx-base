package cn.oyzh.fx.gui.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileApkSVGGlyph extends SVGGlyph {

    public FileApkSVGGlyph() {
        super("/fx-svg/file/a/file-apk.svg");
    }

    public FileApkSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
