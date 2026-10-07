package cn.oyzh.fx.gui.text;

import cn.oyzh.fx.plus.controls.text.FXText;

/**
 * 成功文本
 *
 * @author oyzh
 * @since 2024-12-06
 */
public class SuccessText extends FXText {

    {
        this.addClass("success");
    }

    /**
     * 构造成功文本
     */
    public SuccessText() {
        super("");
    }

    /**
     * 构造成功文本
     *
     * @param text 文本
     */
    public SuccessText(String text) {
        super(text);
    }
}
