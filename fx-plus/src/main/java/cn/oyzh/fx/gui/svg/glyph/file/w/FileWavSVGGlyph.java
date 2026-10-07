package cn.oyzh.fx.gui.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * WAV 音频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileWavSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileWavSVGGlyph() {
        super("/fx-svg/file/w/file-wav.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileWavSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
