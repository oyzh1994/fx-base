package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 删除 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class DeleteSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public DeleteSVGGlyph() {
        super("/fx-svg/delete.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public DeleteSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.delete());
//        super.initNode();
//    }
}
