package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.QuitSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 退出标签
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class QuitSVGLabel extends SVGLabel {

    /**
     * 构造退出标签
     */
    public QuitSVGLabel() {
        this.setGraphic(new QuitSVGGlyph());
    }

    /**
     * 构造退出标签
     *
     * @param size 图标尺寸
     */
    public QuitSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.quit());
//        this.setTipText(I18nHelper.quit());
        super.initNode();
    }
}
