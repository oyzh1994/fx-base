package cn.oyzh.fx.gui.textfield;

import cn.oyzh.fx.gui.skin.ExampleTextFieldSkin;
import javafx.scene.control.Skin;

/**
 * @author oyzh
 * @since 2024/07/04
 */
public class ExampleTextField extends LimitTextField {

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ExampleTextFieldSkin skin() {
        ExampleTextFieldSkin skin = (ExampleTextFieldSkin) this.getSkin();
        if (skin == null) {
            this.setSkin(this.createDefaultSkin());
            skin = (ExampleTextFieldSkin) this.getSkin();
        }
        return skin;
    }

    public void setExample(Object o) {
        if (o != null) {
            this.setExampleText(o.toString());
        }
    }

    public void setExampleText(String exampleText) {
        this.skin().setExampleText(exampleText);
    }

    public String getExampleText() {
        return this.skin().getExampleText();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ExampleTextFieldSkin(this);
    }
}
