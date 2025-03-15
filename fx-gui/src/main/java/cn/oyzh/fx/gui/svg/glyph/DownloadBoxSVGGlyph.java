package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DownloadBoxSVGGlyph extends SVGGlyph {

    public DownloadBoxSVGGlyph() {
        this.setUrl("/fx-svg/download-box.svg");
    }

    public DownloadBoxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
