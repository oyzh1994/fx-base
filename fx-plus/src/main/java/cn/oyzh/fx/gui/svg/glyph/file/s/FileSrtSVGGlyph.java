package cn.oyzh.fx.gui.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SRT 字幕文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-06
 */
public class FileSrtSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileSrtSVGGlyph() {
        super("/fx-svg/file/s/file-srt.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileSrtSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
