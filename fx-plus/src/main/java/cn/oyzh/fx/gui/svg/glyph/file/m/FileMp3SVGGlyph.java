package cn.oyzh.fx.gui.svg.glyph.file.m;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MP3 音频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-06
 */
public class FileMp3SVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileMp3SVGGlyph() {
        super("/fx-svg/file/m/file-mp3.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileMp3SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
