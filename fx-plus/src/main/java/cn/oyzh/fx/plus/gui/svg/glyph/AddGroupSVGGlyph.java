package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class AddGroupSVGGlyph extends SVGGlyph {

    public AddGroupSVGGlyph() {
        this.setUrl("/fx-plus/font/addGroup.svg");
    }

    public AddGroupSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.addGroup());
        super.initNode();
    }
}
