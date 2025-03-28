package cn.oyzh.fx.gui.text;

import cn.oyzh.fx.plus.controls.text.FXText;

/**
 * @author oyzh
 * @since 2024/04/08
 */
public class AccentText extends FXText {

    {
        this.addClass("accent");
    }

    public AccentText() {
        super("");
    }

    public AccentText(String text) {
        super(text);
    }
}
