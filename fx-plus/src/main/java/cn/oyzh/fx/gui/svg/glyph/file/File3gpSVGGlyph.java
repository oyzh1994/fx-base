package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 3GP 视频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class File3gpSVGGlyph extends SVGGlyph {

    /**
     * 构造3GP 视频文件 SVG 图标控件
     */
    public File3gpSVGGlyph() {
        super("/fx-svg/file/file-3gp.svg");
    }

    /**
     * 构造指定尺寸的3GP 视频文件 SVG 图标控件
     *
     * @param size 尺寸
     */
    public File3gpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
