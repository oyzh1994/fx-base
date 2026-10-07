package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.ExpendSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 展开标签
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class ExpandSVGLabel extends SVGLabel {

    /**
     * 构造展开标签
     */
    public ExpandSVGLabel() {
        this.setGraphic(new ExpendSVGGlyph());
    }

    /**
     * 构造展开标签
     *
     * @param size 图标尺寸
     */
    public ExpandSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.expand());
//        this.setTipText(I18nHelper.expand());
        super.initNode();
    }
}
