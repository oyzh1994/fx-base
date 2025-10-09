package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.TestSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.key.GenerateKeySVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 生成密钥按钮
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class GenerateKeyButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.addClass("success");
        this.setText(I18nHelper.generate());
        this.setTipText(I18nHelper.generate());
        this.init(new GenerateKeySVGGlyph(), 0.7);
        super.initNode();
    }
}
