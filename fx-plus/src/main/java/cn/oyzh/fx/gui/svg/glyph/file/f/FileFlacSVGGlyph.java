package cn.oyzh.fx.gui.svg.glyph.file.f;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * FLAC 音频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileFlacSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileFlacSVGGlyph() {
        super("/fx-svg/file/f/file-flac.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileFlacSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
