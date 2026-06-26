package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UploadDownloadSVGGlyph extends ScalingSVGGlyph {

    public UploadDownloadSVGGlyph() {
        super("/fx-svg/upload-download.svg");
    }

    public UploadDownloadSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.875;
    }
}
