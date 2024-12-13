package cn.oyzh.fx.rich.richtextfx.terminal;

import cn.oyzh.fx.rich.richtextfx.control.FlexRichTextArea;
import cn.oyzh.fx.rich.richtextfx.control.RichTextAreaPane;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public abstract class RichTerminalTextAreaPane extends RichTextAreaPane<FlexRichTextArea> {

    public RichTerminalTextAreaPane() {
        super(new RichTerminalTextArea());
    }

}
