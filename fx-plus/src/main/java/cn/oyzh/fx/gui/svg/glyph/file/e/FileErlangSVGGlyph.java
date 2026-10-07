package cn.oyzh.fx.gui.svg.glyph.file.e;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Erlang 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileErlangSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileErlangSVGGlyph() {
        super("/fx-svg/file/e/file-erlang.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileErlangSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
