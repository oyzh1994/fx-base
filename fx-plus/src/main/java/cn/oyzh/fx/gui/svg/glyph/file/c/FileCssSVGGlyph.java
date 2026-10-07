package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * CSS 样式文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCssSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileCssSVGGlyph() {
        super("/fx-svg/file/c/file-css.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileCssSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
