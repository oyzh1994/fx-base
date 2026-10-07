package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.ExampleTextFieldSkin;
import javafx.scene.control.Skin;

/**
 * 示例文本输入框
 *
 * @author oyzh
 * @since 2024-07-05
 */
public class ExampleTextField extends LimitTextField {

    /**
     * 设置示例内容
     *
     * @param o 示例内容
     */
    public void setExample(Object o) {
        if (o != null) {
            this.setExampleText(o.toString());
        }
    }

    /**
     * 设置示例文本
     *
     * @param exampleText 示例文本
     */
    public void setExampleText(String exampleText) {
        this.skin().setExampleText(exampleText);
    }

    /**
     * 获取示例文本
     *
     * @return 示例文本
     */
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
