package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.UploadDownloadSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class UploadDownloadSVGLabel extends SVGLabel {

    public UploadDownloadSVGLabel() {
        this.setGraphic(new UploadDownloadSVGGlyph());
    }

    public UploadDownloadSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

}
