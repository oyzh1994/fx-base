package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 撤销 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class UndoSVGGlyph extends SVGGlyph {

    /**
     * 构造撤销 SVG 图标控件
     */
    public UndoSVGGlyph() {
        super("/fx-svg/data_revoke.svg");
    }

    /**
     * 构造指定尺寸的撤销 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UndoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.undo());
//        super.initNode();
//    }
}
