package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class AddGroupSVGGlyph extends SVGGlyph {

    public AddGroupSVGGlyph() {
        super("/fx-plus/font/addGroup.svg");
    }

    public AddGroupSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("addGroup"));
        super.initNode();
    }
}
