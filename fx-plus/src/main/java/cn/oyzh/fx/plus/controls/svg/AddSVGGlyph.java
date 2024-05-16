package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class AddSVGGlyph extends SVGGlyph {

    public AddSVGGlyph() {
        this.setUrl("/fx-plus/font/add.svg");
    }

    public AddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.add());
        super.initNode();
    }
}
