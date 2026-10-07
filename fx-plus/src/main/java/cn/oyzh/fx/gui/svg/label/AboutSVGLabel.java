package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.AboutSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 关于标签
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class AboutSVGLabel extends SVGLabel {

    /**
     * 构造关于标签
     */
    public AboutSVGLabel() {
        this.setGraphic(new AboutSVGGlyph());
    }

    /**
     * 构造关于标签
     *
     * @param size 图标尺寸
     */
    public AboutSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.about());
//        this.setTipText(I18nHelper.about());
        super.initNode();
    }
}
