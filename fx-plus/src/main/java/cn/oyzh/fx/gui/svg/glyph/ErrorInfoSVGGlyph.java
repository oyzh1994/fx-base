package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 错误信息 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ErrorInfoSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ErrorInfoSVGGlyph() {
        super("/fx-svg/error-info.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ErrorInfoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.file());
//        super.initNode();
//    }
}
