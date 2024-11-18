package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SettingSVGLabel extends SVGLabel {

    public SettingSVGLabel() {
        this.setGraphic(new SettingSVGGlyph());
    }

    public SettingSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.setting());
        this.setTipText(I18nHelper.setting());
        super.initNode();
    }
}
