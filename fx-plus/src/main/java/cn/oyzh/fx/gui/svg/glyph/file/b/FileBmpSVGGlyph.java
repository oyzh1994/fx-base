package cn.oyzh.fx.gui.svg.glyph.file.b;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * BMP 位图图像文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileBmpSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileBmpSVGGlyph() {
        super("/fx-svg/file/b/file-bmp.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileBmpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
