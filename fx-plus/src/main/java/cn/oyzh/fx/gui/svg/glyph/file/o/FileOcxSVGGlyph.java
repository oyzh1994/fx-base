package cn.oyzh.fx.gui.svg.glyph.file.o;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * OCX 控件文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileOcxSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileOcxSVGGlyph() {
        super("/fx-svg/file/o/file-ocx.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileOcxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
