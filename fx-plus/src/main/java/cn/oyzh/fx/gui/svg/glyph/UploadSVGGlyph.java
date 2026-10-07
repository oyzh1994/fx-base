package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 上传 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class UploadSVGGlyph extends SVGGlyph {

    /**
     * 构造上传 SVG 图标控件
     */
    public UploadSVGGlyph() {
        super("/fx-svg/upload.svg");
    }

    /**
     * 构造指定尺寸的上传 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UploadSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.export());
//        super.initNode();
//    }
}
