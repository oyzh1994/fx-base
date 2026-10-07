package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 复制标签
 *
 * @author oyzh
 * @since 2024-04-08
 */
public class CopySVGLabel extends SVGLabel {

    /**
     * 构造复制标签
     */
    public CopySVGLabel() {
        this.setGraphic(new CopySVGGlyph());
    }

    /**
     * 构造复制标签
     *
     * @param size 图标尺寸
     */
    public CopySVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.copy());
//        this.setTipText(I18nHelper.copy());
        super.initNode();
    }
}
