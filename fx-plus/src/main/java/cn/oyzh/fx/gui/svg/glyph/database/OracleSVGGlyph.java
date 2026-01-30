package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class OracleSVGGlyph extends SVGGlyph {

    public OracleSVGGlyph() {
        this.setUrl("/fx-svg/database/oracle.svg");
    }

    public OracleSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
