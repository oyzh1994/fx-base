package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

/**
 * 菜单项
 *
 * @author oyzh
 * @since 2023/3/3
 */
public class FXMenuItem extends MenuItem implements StateAdapter, ThemeAdapter {

    {
        this.setStyle("-fx-padding: 0 0 0 0;");
        this.disableProperty().addListener((observable, oldValue, newValue) -> {
            if (this.getGraphic() != null) {
                this.getGraphic().setDisable(newValue);
            }
        });
        NodeManager.init(this);
    }

    public FXMenuItem(Node graphic) {
        super("", graphic);
    }

    public FXMenuItem(Node graphic, String text, String tipText, Runnable action) {
        super(text, graphic);
        // 设置操作代理
        if (action != null) {
            this.setOnAction(event -> {
                try {
                    action.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        // 设置提示文字
        if (tipText != null) {
            ControlUtil.setTipText(this, tipText);
        }
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     */
    public double getWidth() {
        String str = this.getText();
        Node graphic = this.getGraphic();
        double w = FontUtil.stringWidth(str);
        if (graphic != null) {
            w += graphic.maxWidth(-1);
        }
        return w;
    }

    /**
     * 使用svg和文字来生成菜单项
     *
     * @param text  文字
     * @param glyph svg内容
     * @return 菜单项
     */
    public static FXMenuItem newItem(String text, SVGGlyph glyph) {
        return newItem(text, glyph, null, null);
    }

    /**
     * 使用svg和文字来生成菜单项
     *
     * @param text   文字
     * @param glyph  svg内容
     * @param action 执行业务
     * @return 菜单项
     */
    public static FXMenuItem newItem(String text, SVGGlyph glyph, Runnable action) {
        return newItem(text, glyph, null, action);
    }

    /**
     * 使用svg和文字来生成菜单项
     *
     * @param text    文字
     * @param glyph   svg内容
     * @param tipText 提示文字
     * @param action  执行业务
     * @return 菜单项
     */
    public static FXMenuItem newItem(String text, SVGGlyph glyph, String tipText, Runnable action) {
        // 生成标签
        SVGLabel label = new SVGLabel(text, glyph);
        // 设置边距
        label.setStyle("-fx-padding: 0 0 0 0;");
        // 计算宽度
        double w = FontUtil.stringWidth(text);
        if (glyph != null) {
            w += glyph.getWidth() + 20;
        }
        // 设置宽度
        label.setMaxWidth(w);
        label.setMinWidth(w);
        label.setPrefWidth(w);
        // 生成菜单项
        FXMenuItem item = new FXMenuItem(label);
        // 设置提示文字
        if (tipText != null) {
            ControlUtil.setTipText(item.getGraphic(), tipText);
        }
        // 设置操作
        if (action != null) {
            item.setOnAction(e -> action.run());
        }
        return item;
    }

    /**
     * 使用文字来生成菜单项
     *
     * @param text    文字
     * @param tipText 提示文字
     * @param action  执行业务
     * @return 菜单项
     */
    public static FXMenuItem newItem(String text, String tipText, Runnable action) {
        // 生成菜单项
        FXMenuItem item = new FXMenuItem(null, text, tipText, action);
        // 设置操作
        if (action != null) {
            item.setOnAction(e -> action.run());
        }
        return item;
    }

    /**
     * 使用文字来生成菜单项
     *
     * @param text   文字
     * @param action 执行业务
     * @return 菜单项
     */
    public static FXMenuItem newItem(String text, Runnable action) {
        // 生成菜单项
        FXMenuItem item = new FXMenuItem(null, text, null, action);
        // 设置操作
        if (action != null) {
            item.setOnAction(e -> action.run());
        }
        return item;
    }

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }
}
