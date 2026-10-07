package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.MessageSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 消息标签
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class MessageSVGLabel extends SVGLabel {

    /**
     * 构造消息标签
     */
    public MessageSVGLabel() {
        this.setGraphic(new MessageSVGGlyph());
    }

    /**
     * 构造消息标签
     *
     * @param size 图标尺寸
     */
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
