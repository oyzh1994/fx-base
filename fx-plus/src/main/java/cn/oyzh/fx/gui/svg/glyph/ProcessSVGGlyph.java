package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 进程 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ProcessSVGGlyph extends SVGGlyph {

    /**
     * 构造进程 SVG 图标控件
     */
    public ProcessSVGGlyph() {
        super("/fx-svg/process.svg");
    }

    /**
     * 构造指定尺寸的进程 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ProcessSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
