package cn.oyzh.fx.plus.controls.rich;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.InlineCssTextField;

/**
 * @author oyzh
 * @since 2023/9/15
 */
public class BaseRichTextField extends InlineCssTextField implements NodeAdapter, ThemeAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        if (this.isEnableTheme()) {
            Node placeholder = this.getPlaceholder();
            CaretNode caretNode = this.getCaretSelectionBind().getUnderlyingCaret();
            if (style.isDarkMode()) {
                this.setStyle(0, this.getLength(), "-fx-fill: #fff;");
                caretNode.setStroke(Color.WHITE);
                if (placeholder != null) {
                    placeholder.setStyle("-fx-fill: #fff;");
                }
            } else {
                this.setStyle(0, this.getLength(), "-fx-fill: #000");
                caretNode.setStroke(Color.BLACK);
                if (placeholder != null) {
                    placeholder.setStyle("-fx-fill: #000;");
                }
            }
        }
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
        this.getStyleClass().add("rich-text-field");
    }
}
