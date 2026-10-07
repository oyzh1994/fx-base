package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 设置标签
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class SettingSVGLabel extends SVGLabel {

    /**
     * 构造设置标签
     */
    public SettingSVGLabel() {
        this.setGraphic(new SettingSVGGlyph());
    }

    /**
     * 构造设置标签
     *
     * @param size 图标尺寸
     */
    public SettingSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.setting());
//        this.setTipText(I18nHelper.setting());
        super.initNode();
    }
}
