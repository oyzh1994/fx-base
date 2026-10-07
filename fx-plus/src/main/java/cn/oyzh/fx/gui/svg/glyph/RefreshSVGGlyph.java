package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 刷新 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class RefreshSVGGlyph extends SVGGlyph {

    /**
     * 构造刷新 SVG 图标控件
     */
    public RefreshSVGGlyph() {
        super("/fx-svg/reload.svg");
    }

    /**
     * 构造指定尺寸的刷新 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public RefreshSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.refresh());
//        super.initNode();
//    }
}
