package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 文本文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileTextSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileTextSVGGlyph() {
        super("/fx-svg/file/t/file-text.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileTextSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
