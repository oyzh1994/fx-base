package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.SnippetSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SnippetSVGLabel extends SVGLabel {

    public SnippetSVGLabel() {
        this.setGraphic(new SnippetSVGGlyph());
    }

    public SnippetSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.snippet());
        super.initNode();
    }
}
