package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UploadDownloadSVGGlyph extends SVGGlyph {

    public UploadDownloadSVGGlyph() {
        super("/fx-svg/upload-download.svg");
    }

    public UploadDownloadSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
