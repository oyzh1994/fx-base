package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * TAR 归档文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTarSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileTarSVGGlyph() {
        super("/fx-svg/file/t/file-tar.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileTarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
