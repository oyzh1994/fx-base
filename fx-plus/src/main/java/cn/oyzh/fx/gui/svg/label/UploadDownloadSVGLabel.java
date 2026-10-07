package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.UploadDownloadSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;

/**
 * 上传下载标签
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class UploadDownloadSVGLabel extends SVGLabel {

    /**
     * 构造上传下载标签
     */
    public UploadDownloadSVGLabel() {
        this.setGraphic(new UploadDownloadSVGGlyph());
    }

    /**
     * 构造上传下载标签
     *
     * @param size 图标尺寸
     */
    public UploadDownloadSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

}
