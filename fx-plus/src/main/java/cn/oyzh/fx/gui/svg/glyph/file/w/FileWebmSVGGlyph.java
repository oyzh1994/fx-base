package cn.oyzh.fx.gui.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * WebM 视频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileWebmSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileWebmSVGGlyph() {
        super("/fx-svg/file/w/file-webm.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileWebmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
