package cn.oyzh.fx.rich.richtextfx.terminal;

import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.richtextfx.control.FlexRichTextArea;
import javafx.scene.text.Font;
import lombok.Getter;

/**
 * 富文本数据文本域
 *
 * @author oyzh
 * @since 2024/04/17
 */
@Getter
public class RichTerminalTextArea extends FlexRichTextArea implements NodeAdapter {

//    @Override
//    public void initTextStyle() {
//        FXUtil.runWait(() -> super.changeTheme(ThemeManager.currentTheme()));
//    }

    @Override
    public void initNode() {
        this.disableFont();
        this.initTextStyle();
    }
}
