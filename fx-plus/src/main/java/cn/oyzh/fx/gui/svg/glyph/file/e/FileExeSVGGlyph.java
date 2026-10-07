package cn.oyzh.fx.gui.svg.glyph.file.e;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Windows 可执行文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileExeSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileExeSVGGlyph() {
        super("/fx-svg/file/e/file-exe.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileExeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
