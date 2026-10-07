package cn.oyzh.fx.gui.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 汇编源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileAsmSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileAsmSVGGlyph() {
        super("/fx-svg/file/a/file-asm.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileAsmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
