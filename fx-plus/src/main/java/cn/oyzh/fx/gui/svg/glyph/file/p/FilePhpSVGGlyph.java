package cn.oyzh.fx.gui.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * PHP 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FilePhpSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FilePhpSVGGlyph() {
        super("/fx-svg/file/p/file-php.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FilePhpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
