package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 7Z 压缩文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class File7zSVGGlyph extends SVGGlyph {

    /**
     * 构造7Z 压缩文件 SVG 图标控件
     */
    public File7zSVGGlyph() {
        super("/fx-svg/file/file-7z.svg");
    }

    /**
     * 构造指定尺寸的7Z 压缩文件 SVG 图标控件
     *
     * @param size 尺寸
     */
    public File7zSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
