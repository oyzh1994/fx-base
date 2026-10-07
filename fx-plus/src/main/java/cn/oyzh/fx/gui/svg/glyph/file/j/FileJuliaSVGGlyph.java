package cn.oyzh.fx.gui.svg.glyph.file.j;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Julia 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileJuliaSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileJuliaSVGGlyph() {
        super("/fx-svg/file/j/file-julia.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileJuliaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
