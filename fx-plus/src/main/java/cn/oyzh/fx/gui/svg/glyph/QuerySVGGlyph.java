package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 查询 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class QuerySVGGlyph extends SVGGlyph {

    /**
     * 构造查询 SVG 图标控件
     */
    public QuerySVGGlyph() {
        super("/fx-svg/query.svg");
    }

    /**
     * 构造指定尺寸的查询 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public QuerySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.query());
//        super.initNode();
//    }
}
