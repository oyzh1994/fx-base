package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.CollapseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 折叠标签
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class CollapseSVGLabel extends SVGLabel {

    /**
     * 构造折叠标签
     */
    public CollapseSVGLabel() {
        this.setGraphic(new CollapseSVGGlyph());
    }

    /**
     * 构造折叠标签
     *
     * @param size 图标尺寸
     */
    public CollapseSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.collapse());
        // this.setTipText(I18nResourceBundle.i18nString("base.collapse"));
        super.initNode();
    }
}
