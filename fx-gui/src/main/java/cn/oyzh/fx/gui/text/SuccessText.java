package cn.oyzh.fx.gui.text;

import cn.oyzh.fx.plus.controls.text.FlexText;

/**
 * @author oyzh
 * @since 2024/04/09
 */
public class SuccessText extends FlexText {

    {
        this.addClass("success");
    }

    public SuccessText() {
        super("");
    }

    public SuccessText(String text) {
        super(text);
    }
}
