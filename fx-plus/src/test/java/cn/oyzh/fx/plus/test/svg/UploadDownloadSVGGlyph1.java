package cn.oyzh.fx.plus.test.svg;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 上传下载 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-12-25
 */
public class UploadDownloadSVGGlyph1 extends SVGGlyph {

    /**
     * 构造上传下载 SVG 图标控件
     */
    public UploadDownloadSVGGlyph1() {
        super("/fx-svg/upload-download.svg");
    }

    /**
     * 构造指定尺寸的上传下载 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UploadDownloadSVGGlyph1(String size) {
        this();
        this.setSizeStr(size);
    }
}
