package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 文件夹上传 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-06
 */
public class FolderUploadSVGGlyph extends SVGGlyph {

    /**
     * 构造文件夹上传 SVG 图标控件
     */
    public FolderUploadSVGGlyph() {
        super("/fx-svg/file/folder-upload.svg");
    }

    /**
     * 构造指定尺寸的文件夹上传 SVG 图标控件
     *
     * @param size 尺寸
     */
    public FolderUploadSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
