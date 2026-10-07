package cn.oyzh.fx.gui.svg.glyph.file.j;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * JPEG 图像文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-06
 */
public class FileJpgSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileJpgSVGGlyph() {
        super("/fx-svg/file/j/file-jpg.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileJpgSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
