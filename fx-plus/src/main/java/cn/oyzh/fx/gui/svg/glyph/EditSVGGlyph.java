package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 编辑 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class EditSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public EditSVGGlyph() {
        super("/fx-svg/edit.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public EditSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.edit());
//        super.initNode();
//    }
}
