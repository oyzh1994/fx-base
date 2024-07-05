package cn.oyzh.fx.rich.terminal;

import cn.oyzh.fx.rich.control.FlexRichTextArea;
import cn.oyzh.fx.rich.control.RichTextAreaPane;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public abstract class RichTerminalTextAreaPane extends RichTextAreaPane<FlexRichTextArea> {

    public RichTerminalTextAreaPane() {
        super(new FlexRichTextArea());
    }
}
