package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 停止 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class StopSVGGlyph extends SVGGlyph {

    /**
     * 构造停止 SVG 图标控件
     */
    public StopSVGGlyph() {
        super("/fx-svg/stop-circle-line.svg");
    }

    /**
     * 构造指定尺寸的停止 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public StopSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.stop());
//        super.initNode();
//    }
}
