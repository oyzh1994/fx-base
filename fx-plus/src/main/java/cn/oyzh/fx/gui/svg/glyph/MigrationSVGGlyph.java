package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MigrationSVGGlyph extends SVGGlyph {

    public MigrationSVGGlyph() {
        this.setUrl("/fx-svg/migration.svg");
    }

    public MigrationSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
