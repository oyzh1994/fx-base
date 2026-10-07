package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * MongoDB 数据库 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-09-05
 */
public class MongodbSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造MongoDB 数据库 SVG 图标控件
     */
    public MongodbSVGGlyph() {
        super("/fx-svg/database/mongodb.svg");
    }

    /**
     * 构造指定尺寸的MongoDB 数据库 SVG 图标控件
     *
     * @param size 尺寸
     */
    public MongodbSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double widthScaling() {
        return 0.7;
    }
}
