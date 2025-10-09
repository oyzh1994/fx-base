package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.CloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ExecuteSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class ExecuteButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.setText(I18nHelper.execute());
        this.setTipText(I18nHelper.execute());
        this.init(new ExecuteSVGGlyph(), 0.7);
        this.setOnAction(actionEvent -> this.getScene().getWindow().hide());
        super.initNode();
    }
}
