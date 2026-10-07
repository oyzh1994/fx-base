package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Java 字节码文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileClassSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileClassSVGGlyph() {
        super("/fx-svg/file/c/file-class.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileClassSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
