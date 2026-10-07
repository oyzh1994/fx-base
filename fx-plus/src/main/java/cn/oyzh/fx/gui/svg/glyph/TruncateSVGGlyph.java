package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 截断 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-07-26
 */
public class TruncateSVGGlyph extends SVGGlyph {

    /**
     * 构造截断 SVG 图标控件
     */
    public TruncateSVGGlyph() {
        super("/fx-svg/truncate.svg");
    }

    /**
     * 构造指定尺寸的截断 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public TruncateSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.truncate());
//        super.initNode();
//    }
}
