package cn.oyzh.fx.gui.text;

import cn.oyzh.fx.plus.controls.text.FXText;

/**
 * @author oyzh
 * @since 2024/04/09
 */
public class SuccessText extends FXText {

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
