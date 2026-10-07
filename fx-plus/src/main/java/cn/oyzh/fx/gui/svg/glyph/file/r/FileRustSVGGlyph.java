package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Rust 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRustSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileRustSVGGlyph() {
        super("/fx-svg/file/r/file-rust.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileRustSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
