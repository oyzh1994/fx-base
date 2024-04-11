package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class SettingSVGGlyph extends SVGGlyph {

    public SettingSVGGlyph() {
        super("/fx-plus/font/setting.svg");
    }

    public SettingSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("setting"));
        super.initNode();
    }
}
