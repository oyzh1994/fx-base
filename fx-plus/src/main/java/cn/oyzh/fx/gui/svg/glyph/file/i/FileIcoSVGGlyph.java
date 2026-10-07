package cn.oyzh.fx.gui.svg.glyph.file.i;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 图标文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileIcoSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileIcoSVGGlyph() {
        super("/fx-svg/file/i/file-ico.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileIcoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
