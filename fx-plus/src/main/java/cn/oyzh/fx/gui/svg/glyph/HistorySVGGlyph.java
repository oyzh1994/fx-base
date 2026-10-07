package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 历史 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class HistorySVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public HistorySVGGlyph() {
        super("/fx-svg/history.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public HistorySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.his());
//        super.initNode();
//    }
}
