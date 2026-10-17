package cn.oyzh.fx.plus.test.svg;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;

/**
 * 上传下载 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-12-25
 */
public class UploadDownloadSVGLabel1 extends SVGLabel {

    /**
     * 构造上传下载 SVG 图标控件
     */
    public UploadDownloadSVGLabel1() {
        super("测试");
        super.setUrl("/fx-svg/upload-download.svg");
    }

    /**
     * 构造指定尺寸的上传下载 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UploadDownloadSVGLabel1(String size) {
        this();
        this.setSizeStr(size);
    }
}
