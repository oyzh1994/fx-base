package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 退出 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class QuitSVGGlyph extends SVGGlyph {

    /**
     * 构造退出 SVG 图标控件
     */
    public QuitSVGGlyph() {
        super("/fx-svg/poweroff.svg");
    }

    /**
     * 构造指定尺寸的退出 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public QuitSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.quit());
//        super.initNode();
//    }
}
