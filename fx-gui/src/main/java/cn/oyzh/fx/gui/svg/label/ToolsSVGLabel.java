package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.ToolsSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ToolsSVGLabel extends SVGLabel {

    public ToolsSVGLabel() {
        this.setGraphic(new ToolsSVGGlyph());
    }

    public ToolsSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.tools());
//        this.setTipText(I18nHelper.tools());
        super.initNode();
    }
}
