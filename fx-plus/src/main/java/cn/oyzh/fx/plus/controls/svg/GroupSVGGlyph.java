package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class GroupSVGGlyph extends SVGGlyph {

    public GroupSVGGlyph() {
        this.setUrl("/fx-plus/font/group.svg");
    }

    public GroupSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.group"));
        super.initNode();
    }
}
