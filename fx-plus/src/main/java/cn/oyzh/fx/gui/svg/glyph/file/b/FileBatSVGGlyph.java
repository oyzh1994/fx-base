package cn.oyzh.fx.gui.svg.glyph.file.b;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Windows 批处理文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileBatSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileBatSVGGlyph() {
        super("/fx-svg/file/b/file-bat.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileBatSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
