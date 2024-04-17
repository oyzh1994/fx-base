package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class CollapseSVGLabel extends SVGLabel {

    public CollapseSVGLabel() {
        this.setGraphic(new CollapseSVGGlyph());
    }

    public CollapseSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("base.collapse"));
        this.setTipText(BaseResourceBundle.getBaseString("base.collapse"));
        super.initNode();
    }
}
