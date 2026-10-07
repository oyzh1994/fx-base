package cn.oyzh.fx.gui.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * WebP 图像文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWebpSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileWebpSVGGlyph() {
        super("/fx-svg/file/w/file-webp.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileWebpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
