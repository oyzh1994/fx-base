package cn.oyzh.fx.gui.svg.glyph.file.k;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Kotlin 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileKtSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileKtSVGGlyph() {
        super("/fx-svg/file/k/file-kt.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileKtSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
