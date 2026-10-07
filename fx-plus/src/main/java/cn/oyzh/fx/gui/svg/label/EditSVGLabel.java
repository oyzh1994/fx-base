package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.EditSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 编辑标签
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class EditSVGLabel extends SVGLabel {

    /**
     * 构造编辑标签
     */
    public EditSVGLabel() {
        this.setGraphic(new EditSVGGlyph());
    }

    /**
     * 构造编辑标签
     *
     * @param size 图标尺寸
     */
    public EditSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.edit());
//        this.setTipText(I18nHelper.edit());
        super.initNode();
    }
}
