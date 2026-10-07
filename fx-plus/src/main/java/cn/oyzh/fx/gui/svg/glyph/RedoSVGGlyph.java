package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 重做 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class RedoSVGGlyph extends SVGGlyph {

    /**
     * 构造重做 SVG 图标控件
     */
    public RedoSVGGlyph() {
        super("/fx-svg/data_redo.svg");
    }

    /**
     * 构造指定尺寸的重做 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public RedoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.redo());
//        super.initNode();
//    }
}
