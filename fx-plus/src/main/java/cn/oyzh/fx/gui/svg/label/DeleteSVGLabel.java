package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.DeleteSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 删除标签
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class DeleteSVGLabel extends SVGLabel {

    /**
     * 构造删除标签
     */
    public DeleteSVGLabel() {
        this.setGraphic(new DeleteSVGGlyph());
    }

    /**
     * 构造删除标签
     *
     * @param size 图标尺寸
     */
    public DeleteSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.delete());
//        this.setTipText(I18nHelper.delete());
        super.initNode();
    }
}
