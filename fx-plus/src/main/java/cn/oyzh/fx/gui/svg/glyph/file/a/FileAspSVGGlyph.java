package cn.oyzh.fx.gui.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * ASP 文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileAspSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileAspSVGGlyph() {
        super("/fx-svg/file/a/file-asp.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileAspSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
