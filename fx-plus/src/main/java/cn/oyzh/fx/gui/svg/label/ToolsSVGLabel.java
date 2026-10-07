package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.ToolsSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 工具标签
 *
 * @author oyzh
 * @since 2024-04-08
 */
public class ToolsSVGLabel extends SVGLabel {

    /**
     * 构造工具标签
     */
    public ToolsSVGLabel() {
        this.setGraphic(new ToolsSVGGlyph());
    }

    /**
     * 构造工具标签
     *
     * @param size 图标尺寸
     */
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
