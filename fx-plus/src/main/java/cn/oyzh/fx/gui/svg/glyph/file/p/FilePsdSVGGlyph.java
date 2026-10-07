package cn.oyzh.fx.gui.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * PSD 图像文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePsdSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FilePsdSVGGlyph() {
        super("/fx-svg/file/p/file-psd.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FilePsdSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
