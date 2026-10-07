package cn.oyzh.fx.gui.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 共享库文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileSoSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileSoSVGGlyph() {
        super("/fx-svg/file/s/file-so.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileSoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
