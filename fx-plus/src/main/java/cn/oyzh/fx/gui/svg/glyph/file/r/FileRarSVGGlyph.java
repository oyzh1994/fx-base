package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * RAR 压缩文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRarSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileRarSVGGlyph() {
        super("/fx-svg/file/r/file-rar.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileRarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
