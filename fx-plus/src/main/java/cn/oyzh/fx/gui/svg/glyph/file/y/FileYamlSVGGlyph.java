package cn.oyzh.fx.gui.svg.glyph.file.y;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileYamlSVGGlyph extends SVGGlyph {

    public FileYamlSVGGlyph() {
        super("/fx-svg/file/y/file-yaml.svg");
    }

    public FileYamlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
