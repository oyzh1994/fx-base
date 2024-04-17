package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class FileSVGGlyph extends SVGGlyph {

    public FileSVGGlyph() {
        this.setUrl("/fx-plus/font/file.svg");
    }

    public FileSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.file"));
        super.initNode();
    }
}
