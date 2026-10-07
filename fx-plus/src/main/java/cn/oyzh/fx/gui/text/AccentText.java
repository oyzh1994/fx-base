package cn.oyzh.fx.gui.text;

import cn.oyzh.fx.plus.controls.text.FXText;

/**
 * 强调色文本
 *
 * @author oyzh
 * @since 2024-12-06
 */
public class AccentText extends FXText {

    {
        this.addClass("accent");
    }

    /**
     * 构造强调色文本
     */
    public AccentText() {
        super("");
    }

    /**
     * 构造强调色文本
     *
     * @param text 文本
     */
    public AccentText(String text) {
        super(text);
    }
}
