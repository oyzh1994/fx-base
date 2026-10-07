package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 保存 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class SaveSVGGlyph extends SVGGlyph {

    /**
     * 构造保存 SVG 图标控件
     */
    public SaveSVGGlyph() {
        super("/fx-svg/save.svg");
    }

    /**
     * 构造指定尺寸的保存 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SaveSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.save());
//        super.initNode();
//    }
}
