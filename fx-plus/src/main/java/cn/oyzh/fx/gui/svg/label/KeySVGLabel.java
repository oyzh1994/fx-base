package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.key.KeySVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class KeySVGLabel extends SVGLabel {

    public KeySVGLabel() {
        this.setGraphic(new KeySVGGlyph());
    }

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
