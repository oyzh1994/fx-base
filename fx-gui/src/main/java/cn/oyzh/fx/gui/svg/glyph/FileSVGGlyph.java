package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class FileSVGGlyph extends SVGGlyph {

    public FileSVGGlyph() {
        this.setUrl("/fx-gui/font/file.svg");
    }

    public FileSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.file());
        super.initNode();
    }
}
