package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UploadBoxSVGGlyph extends SVGGlyph {

    public UploadBoxSVGGlyph() {
        this.setUrl("/fx-svg/upload-box.svg");
    }

    public UploadBoxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
