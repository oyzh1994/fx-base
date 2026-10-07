package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 终端 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class TerminalSVGGlyph extends SVGGlyph {

    /**
     * 构造终端 SVG 图标控件
     */
    public TerminalSVGGlyph() {
        super("/fx-svg/code-library.svg");
    }

    /**
     * 构造指定尺寸的终端 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public TerminalSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.terminal());
//        super.initNode();
//    }
}
