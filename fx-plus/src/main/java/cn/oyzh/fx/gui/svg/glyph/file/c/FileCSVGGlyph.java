package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * C 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileCSVGGlyph() {
        super("/fx-svg/file/c/file-c.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileCSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
