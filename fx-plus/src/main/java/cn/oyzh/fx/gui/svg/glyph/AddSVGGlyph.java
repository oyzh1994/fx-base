package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 新增 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class AddSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public AddSVGGlyph() {
        super("/fx-svg/add.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public AddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.add());
//        super.initNode();
//    }
}
