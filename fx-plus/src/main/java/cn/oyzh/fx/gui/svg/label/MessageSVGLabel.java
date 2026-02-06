package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.MessageSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class MessageSVGLabel extends SVGLabel {

    public MessageSVGLabel() {
        this.setGraphic(new MessageSVGGlyph());
    }

    public MessageSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.message());
        super.initNode();
    }
}
