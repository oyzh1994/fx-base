package cn.oyzh.fx.gui.svg.glyph.file.g;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * GIF 图像文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileGifSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileGifSVGGlyph() {
        super("/fx-svg/file/g/file-gif.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileGifSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
