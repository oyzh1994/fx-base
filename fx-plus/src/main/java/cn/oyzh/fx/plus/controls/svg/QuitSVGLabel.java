package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class QuitSVGLabel extends SVGLabel {

    public QuitSVGLabel() {
        this.setGraphic(new QuitSVGGlyph());
    }

    public QuitSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("base.quit"));
        this.setTipText(BaseResourceBundle.getBaseString("base.quit"));
        super.initNode();
    }
}
