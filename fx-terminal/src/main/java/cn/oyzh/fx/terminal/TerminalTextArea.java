package cn.oyzh.fx.terminal;

import cn.oyzh.fx.rich.richtextfx.control.BaseRichTextArea;
import lombok.Getter;

/**
 * 富文本数据文本域
 *
 * @author oyzh
 * @since 2024/04/17
 */
@Getter
public class TerminalTextArea extends BaseRichTextArea {

    @Override
    public void initNode() {
        this.disableFont();
        this.initTextStyle();
    }
}
