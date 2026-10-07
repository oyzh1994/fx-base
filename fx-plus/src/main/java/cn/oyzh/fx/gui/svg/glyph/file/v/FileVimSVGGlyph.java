package cn.oyzh.fx.gui.svg.glyph.file.v;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Vim 脚本文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileVimSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileVimSVGGlyph() {
        super("/fx-svg/file/v/file-vim.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileVimSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
