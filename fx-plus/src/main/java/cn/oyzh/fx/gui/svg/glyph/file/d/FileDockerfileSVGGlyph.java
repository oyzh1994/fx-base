package cn.oyzh.fx.gui.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileDockerfileSVGGlyph extends SVGGlyph {

    public FileDockerfileSVGGlyph() {
        super("/fx-svg/file/d/file-dockerfile.svg");
    }

    public FileDockerfileSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
