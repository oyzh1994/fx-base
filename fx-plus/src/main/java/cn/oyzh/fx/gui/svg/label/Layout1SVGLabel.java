package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.layout.Layout1SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 布局1标签
 *
 * @author oyzh
 * @since 2024-04-08
 */
public class Layout1SVGLabel extends SVGLabel {

    /**
     * 构造布局1标签
     */
    public Layout1SVGLabel() {
        this.setGraphic(new Layout1SVGGlyph());
    }

    /**
     * 构造布局1标签
     *
     * @param size 图标尺寸
     */
    public Layout1SVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.layout() + "1");
        super.initNode();
    }
}
