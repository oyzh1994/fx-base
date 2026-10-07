package cn.oyzh.fx.gui.svg.glyph.file.f;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * F# 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileFsharpSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileFsharpSVGGlyph() {
        super("/fx-svg/file/f/file-fsharp.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileFsharpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
