package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.ApplySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SaveSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 保存按钮
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class ApplyButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.setText(I18nHelper.apply());
        this.setTipText(I18nHelper.apply());
        this.init(new ApplySVGGlyph());
        super.initNode();
    }
}
