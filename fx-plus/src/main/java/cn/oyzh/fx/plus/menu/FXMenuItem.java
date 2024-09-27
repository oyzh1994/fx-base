package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.common.util.Destroyable;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

import java.lang.ref.Cleaner;

/**
 * 菜单项
 *
 * @author oyzh
 * @since 2023/3/3
 */
public class FXMenuItem extends MenuItem implements StateAdapter, ThemeAdapter, Destroyable {

    private ChangeListener<Boolean> disableListener = (observable, oldValue, newValue) -> {
        if (this.getGraphic() != null) {
            this.getGraphic().setDisable(newValue);
        }
    };

    {
        this.setStyle("-fx-padding: 0 0 0 0;");
        this.disableProperty().addListener(this.disableListener);
        NodeManager.init(this);
    }

    public FXMenuItem(Node graphic, String text, Runnable action) {
        if (text != null) {
            super.setText(text);
        }
        if (graphic != null) {
            super.setGraphic(graphic);
        }
        // 设置操作代理
        if (action != null) {
            this.setOnAction(event -> action.run());
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
     * @param text   文字
     * @param glyph  svg内容
     * @param action 执行业务
     * @return 菜单项
     */
    public static FXMenuItem newItem(String text, SVGGlyph glyph, Runnable action) {
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
        return new FXMenuItem(label, null, action);
    }

    /**
     * 使用文字来生成菜单项
     *
     * @param text   文字
     * @param action 执行业务
     * @return 菜单项
     */
    public static FXMenuItem newItem(String text, Runnable action) {
        return new FXMenuItem(null, text, action);
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
    public void destroy() {
        this.disableProperty().removeListener(this.disableListener);
        this.disableListener = null;
        this.setText(null);
        this.setStyle(null);
        this.setGraphic(null);
        this.setOnAction(null);
        this.clearProps();
    }
}
