package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

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
        this.setText(BaseResourceBundle.getBaseString("base.setting"));
        this.setTipText(BaseResourceBundle.getBaseString("base.setting"));
        super.initNode();
    }
}
