package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class DamengSVGGlyph extends SVGGlyph {

    public DamengSVGGlyph() {
        super("/fx-svg/database/dameng.svg");
    }

    public DamengSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
