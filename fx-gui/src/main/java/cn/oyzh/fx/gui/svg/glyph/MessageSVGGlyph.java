package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MessageSVGGlyph extends SVGGlyph {

    public MessageSVGGlyph() {
        this.setUrl("/fx-svg/message.svg");
    }

    public MessageSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.message());
//        super.initNode();
//    }
}
