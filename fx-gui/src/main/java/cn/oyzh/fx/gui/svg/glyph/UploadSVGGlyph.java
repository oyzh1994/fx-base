package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UploadSVGGlyph extends SVGGlyph {

    public UploadSVGGlyph() {
        this.setUrl("/fx-svg/upload.svg");
    }

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
