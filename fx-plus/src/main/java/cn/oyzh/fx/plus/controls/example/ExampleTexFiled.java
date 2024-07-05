package cn.oyzh.fx.plus.controls.example;

import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;

/**
 * @author oyzh
 * @since 2024/07/04
 */
public class ExampleTexFiled extends ClearableTextField {

    {
        this.setEditable(false);
        this.setSkin(new ExampleTextFieldSkin(this));
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ExampleTextFieldSkin skin() {
        return (ExampleTextFieldSkin) this.getSkin();
    }

    public void setExampleText(String exampleText) {
        this.skin().setExampleText(exampleText);
    }

    public String getExampleText() {
        return this.skin().getExampleText();
    }
}
