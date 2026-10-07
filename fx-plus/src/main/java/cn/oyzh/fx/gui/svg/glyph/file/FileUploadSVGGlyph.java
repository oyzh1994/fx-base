package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 文件上传 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileUploadSVGGlyph extends SVGGlyph {

    /**
     * 构造文件上传 SVG 图标控件
     */
    public FileUploadSVGGlyph() {
        super("/fx-svg/file/file-upload.svg");
    }

    /**
     * 构造指定尺寸的文件上传 SVG 图标控件
     *
     * @param size 尺寸
     */
    public FileUploadSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
