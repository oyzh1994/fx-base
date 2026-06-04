package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class MongodbSVGGlyph extends ScalingSVGGlyph {

    public MongodbSVGGlyph() {
        super("/fx-svg/database/mongodb.svg");
    }

    public MongodbSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double widthScaling() {
        return 0.6;
    }
}
