package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.FilterSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 过滤标签
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class FilterSVGLabel extends SVGLabel {

    /**
     * 构造过滤标签
     */
    public FilterSVGLabel() {
        this.setGraphic(new FilterSVGGlyph());
    }

    /**
     * 构造过滤标签
     *
     * @param size 图标尺寸
     */
    public FilterSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.filter());
//        this.setTipText(I18nHelper.filter());
        super.initNode();
    }
}
