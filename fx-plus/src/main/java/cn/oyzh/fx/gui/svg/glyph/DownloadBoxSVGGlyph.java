package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 下载盒子 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class DownloadBoxSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public DownloadBoxSVGGlyph() {
        super("/fx-svg/download-box.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public DownloadBoxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
