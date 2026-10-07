package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.TransportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 传输标签
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class TransportSVGLabel extends SVGLabel {

    /**
     * 构造传输标签
     */
    public TransportSVGLabel() {
        this.setGraphic(new TransportSVGGlyph());
    }

    /**
     * 构造传输标签
     *
     * @param size 图标尺寸
     */
    public TransportSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.transport());
//        this.setTipText(I18nHelper.transport());
        super.initNode();
    }
}
