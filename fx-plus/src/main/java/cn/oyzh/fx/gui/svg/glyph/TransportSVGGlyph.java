package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 传输 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class TransportSVGGlyph extends SVGGlyph {

    /**
     * 构造传输 SVG 图标控件
     */
    public TransportSVGGlyph() {
        super("/fx-svg/arrow-left-right-line.svg");
    }

    /**
     * 构造指定尺寸的传输 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public TransportSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.transport());
//        super.initNode();
//    }
}
