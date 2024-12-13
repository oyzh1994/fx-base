package cn.oyzh.fx.rich.richtextfx.terminal;

import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.richtextfx.control.FlexRichTextArea;
import lombok.Getter;

/**
 * 富文本数据文本域
 *
 * @author oyzh
 * @since 2024/04/17
 */
@Getter
public class RichTerminalTextArea extends FlexRichTextArea {

    {
        this.initTextStyle();
    }

    @Override
    public void initTextStyle() {
        FXUtil.runWait(() -> super.changeTheme(ThemeManager.currentTheme()));
    }
}
