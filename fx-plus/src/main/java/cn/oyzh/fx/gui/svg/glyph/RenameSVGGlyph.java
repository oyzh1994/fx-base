package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 重命名 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class RenameSVGGlyph extends SVGGlyph {

    /**
     * 构造重命名 SVG 图标控件
     */
    public RenameSVGGlyph() {
        super("/fx-svg/edit-square.svg");
    }

    /**
     * 构造指定尺寸的重命名 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public RenameSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.rename());
//        super.initNode();
//    }
}
