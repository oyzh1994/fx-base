package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.key.KeySVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 密钥标签
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class KeySVGLabel extends SVGLabel {

    /**
     * 构造密钥标签
     */
    public KeySVGLabel() {
        this.setGraphic(new KeySVGGlyph());
    }

    /**
     * 构造密钥标签
     *
     * @param size 图标尺寸
     */
    public KeySVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.key1());
        super.initNode();
    }
}
