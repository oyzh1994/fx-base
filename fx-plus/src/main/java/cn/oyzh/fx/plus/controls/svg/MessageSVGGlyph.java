package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MessageSVGGlyph extends SVGGlyph {

    public MessageSVGGlyph() {
        this.setUrl("/fx-plus/font/message.svg");
    }

    public MessageSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.message"));
        super.initNode();
    }
}
