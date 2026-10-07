package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 上传下载 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class UploadDownloadSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造上传下载 SVG 图标控件
     */
    public UploadDownloadSVGGlyph() {
        super("/fx-svg/upload-download.svg");
    }

    /**
     * 构造指定尺寸的上传下载 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UploadDownloadSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.875;
    }
}
