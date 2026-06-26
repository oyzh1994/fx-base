package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.ExampleTextFieldSkin;
import javafx.scene.control.Skin;

/**
 * @author oyzh
 * @since 2024/07/04
 */
public class ExampleTextField extends LimitTextField {

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
    public ExampleTextFieldSkin skin() {
        return (ExampleTextFieldSkin) super.skin();
    }

    @Override
    protected ExampleTextFieldSkin createDefaultSkin() {
        return new ExampleTextFieldSkin(this);
    }
}
