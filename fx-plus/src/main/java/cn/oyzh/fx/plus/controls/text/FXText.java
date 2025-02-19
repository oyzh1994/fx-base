package cn.oyzh.fx.plus.controls.text;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.text.Text;

/**
 * @author oyzh
 * @since 2023/04/25
 */
public class FXText extends Text implements StateAdapter, TipAdapter, ThemeAdapter, TextAdapter, FontAdapter, LayoutAdapter, NodeAdapter {

    {
        NodeManager.init(this);
    }

    public FXText() {
        super();
    }

    public FXText(String text) {
        super(text);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        if (this.isEnableTheme()) {
            ThemeAdapter.super.changeTheme(style);
            this.setFill(ThemeManager.currentForegroundColor());
//            if (style.isDarkMode()) {
//                this.setFill(Color.WHITE);
//            } else {
//                this.setFill(Color.BLACK);
//            }
        }
    }

    public void text(String text) {
        FXUtil.runWait(() -> super.setText(text));
    }

    public void clear() {
        this.setText("");
    }
}
