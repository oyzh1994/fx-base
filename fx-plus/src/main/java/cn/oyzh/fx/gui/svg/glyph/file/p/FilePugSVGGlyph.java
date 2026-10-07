package cn.oyzh.fx.gui.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Pug 模板文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePugSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FilePugSVGGlyph() {
        super("/fx-svg/file/p/file-pug.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FilePugSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
