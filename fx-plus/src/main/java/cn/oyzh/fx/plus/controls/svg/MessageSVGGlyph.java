package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MessageSVGGlyph extends SVGGlyph {

    public MessageSVGGlyph() {
        super("/fx-plus/font/message.svg");
    }

    public MessageSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("message"));
        super.initNode();
    }
}
