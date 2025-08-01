package cn.oyzh.fx.terminal;

import cn.oyzh.fx.rich.richtextfx.control.BaseRichTextArea;

/**
 * 富文本数据文本域
 *
 * @author oyzh
 * @since 2024/04/17
 */
public class TerminalTextArea extends BaseRichTextArea {

    @Override
    public void initNode() {
        super.initNode();
        this.disableFont();
        // this.initTextStyle();
    }
}
