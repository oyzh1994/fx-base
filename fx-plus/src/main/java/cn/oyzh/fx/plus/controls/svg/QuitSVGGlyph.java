package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class QuitSVGGlyph extends SVGGlyph {

    public QuitSVGGlyph() {
        super("/fx-plus/font/poweroff.svg");
    }

    public QuitSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("quit"));
        super.initNode();
    }
}
