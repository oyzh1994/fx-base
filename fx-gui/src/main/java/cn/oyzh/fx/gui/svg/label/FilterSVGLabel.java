package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.gui.svg.glyph.FilterSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class FilterSVGLabel extends SVGLabel {

    public FilterSVGLabel() {
        this.setGraphic(new FilterSVGGlyph());
    }

    public FilterSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.filter());
        this.setTipText(I18nHelper.filter());
        super.initNode();
    }
}
