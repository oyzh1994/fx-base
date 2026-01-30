package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.GenerateSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;

/**
 * 生成密钥按钮
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class GenerateButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.addClass("success");
        this.init(new GenerateSVGGlyph(), 0.7);
        super.initNode();
    }
}
