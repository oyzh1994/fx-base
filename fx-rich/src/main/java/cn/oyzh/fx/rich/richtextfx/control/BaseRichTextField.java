package cn.oyzh.fx.rich.richtextfx.control;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.InlineCssTextField;

/**
 * @author oyzh
 * @since 2023/9/15
 */
@Deprecated
public class BaseRichTextField extends InlineCssTextField implements FlexAdapter, NodeAdapter, ThemeAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 清除文字样式
     */
    public void clearTextStyle() {
        FXUtil.runWait(() -> this.setStyle(0, this.getLength(), ""));
    }

    /**
     * 初始化文字样式
     */
    public void initTextStyle() {
        FXUtil.runWait(() -> {
            this.clearTextStyle();
            // 初始化颜色
            if (this.isEnableTheme()) {
                Node placeholder = this.getPlaceholder();
                CaretNode caretNode = this.getCaretSelectionBind().getUnderlyingCaret();
                Color accentColor = ThemeManager.currentAccentColor();
                Color foregroundColor = ThemeManager.currentForegroundColor();
                String fgColor = FXColorUtil.getColorHex(foregroundColor);
                this.setStyle(0, this.getLength(), "-fx-fill: " + fgColor + ";");
                caretNode.setStroke(accentColor);
                if (placeholder != null) {
                    placeholder.setStyle("-fx-fill: " + fgColor + ";");
                }
            }
        });
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        // if (this.isEnableTheme()) {
        //     Node placeholder = this.getPlaceholder();
        //     CaretNode caretNode = this.getCaretSelectionBind().getUnderlyingCaret();
        //     if (style.isDarkMode()) {
        //         this.setStyle(0, this.getLength(), "-fx-fill: #fff;");
        //         caretNode.setStroke(Color.WHITE);
        //         if (placeholder != null) {
        //             placeholder.setStyle("-fx-fill: #fff;");
        //         }
        //     } else {
        //         this.setStyle(0, this.getLength(), "-fx-fill: #000");
        //         caretNode.setStroke(Color.BLACK);
        //         if (placeholder != null) {
        //             placeholder.setStyle("-fx-fill: #000;");
        //         }
        //     }
        // }
        this.initTextStyle();
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
//        this.setFocusTraversable(false);
        this.getStyleClass().add("rich-text-field");
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
