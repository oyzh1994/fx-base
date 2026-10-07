package cn.oyzh.fx.gui.svg.glyph.file.o;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * OGG 音频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileOggSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileOggSVGGlyph() {
        super("/fx-svg/file/o/file-ogg.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileOggSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
