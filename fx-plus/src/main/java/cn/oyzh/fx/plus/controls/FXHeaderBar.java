package cn.oyzh.fx.plus.controls;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.ScreenUtil;
import javafx.application.ColorScheme;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderDragType;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * 标题栏控件，继承自 HeaderBar，由左侧内容、中间标题、右侧操作区组成，支持主题适配
 *
 * @author oyzh
 * @since 2025-08-19
 */
public class FXHeaderBar extends HeaderBar implements NodeAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 获取内容组件
     *
     * @return 内容组件
     */
    public Node getContent() {
        return this.getLeft();
    }

    /**
     * 设置内容组件
     *
     * @param content 内容组件
     */
    public void setContent(Node content) {
        this.setLeft(content);
    }

    /**
     * 获取标题组件
     *
     * @return 标题组件
     */
    private FXLabel getTitleLabel() {
        return (FXLabel) this.getCenter();
    }

    /**
     * 标题内边距
     */
    private static final Insets TITLE_PADDING = new Insets(-3, 0, 0, 20);

    /**
     * 设置标题组件
     *
     * @param label 标题组件
     */
    private void setTitleLabel(FXLabel label) {
        label.setPadding(TITLE_PADDING);
        if (!OSUtil.isLinux()) {
            label.setMouseTransparent(true);
        }
        double w = ScreenUtil.getAllWidth();
        label.setPrefWidth(w);
        label.setFontWeight(FontWeight.BOLD);
        this.setCenter(label);
        // linux要额外配置
        if (OSUtil.isLinux()) {
            HeaderBar.setDragType(label, HeaderDragType.TRANSPARENT_SUBTREE);
            label.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getClickCount() == 2) {
                    Stage stage = this.stage();
                    stage.setMaximized(!stage.isMaximized());
                }
            });
        }
    }

    /**
     * 初始化标题组件
     *
     * @return 标题组件
     */
    private FXLabel initTitleLabel() {
        FXLabel label = this.getTitleLabel();
        if (label == null) {
            label = new FXLabel();
            this.setTitleLabel(label);
        }
        return label;
    }

    /**
     * 获取图标
     *
     * @return 图标
     */
    public Node getIcon() {
        FXLabel label = this.getTitleLabel();
        return label == null ? null : label.getGraphic();
    }

    /**
     * 设置图标
     *
     * @param icon 图标
     */
    public void setIcon(Node icon) {
        FXLabel label = this.initTitleLabel();
        label.setGraphic(icon);
    }

    /**
     * 获取标题
     *
     * @return 标题
     */
    public String getTitle() {
        // this.initCenter();
        FXLabel label = this.getTitleLabel();
        return label == null ? null : label.getText();
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        FXLabel label = this.initTitleLabel();
        label.text(title);
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        Stage stage = this.stage();
        if (stage != null) {
            HeaderBar.setSystemButtonHeight(stage, this.getHeight());
            if (style.isDarkMode()) {
                HeaderBar.setSystemColorScheme(stage, ColorScheme.DARK);
            } else {
                HeaderBar.setSystemColorScheme(stage, ColorScheme.LIGHT);
            }
        }
        if (this.getLeft() instanceof ThemeAdapter adapter) {
            adapter.changeTheme(style);
        }
        if (this.getRight() instanceof ThemeAdapter adapter) {
            adapter.changeTheme(style);
        }
        if (this.getCenter() instanceof ThemeAdapter adapter) {
            adapter.changeTheme(style);
        }
        ThemeAdapter.super.changeTheme(style);
    }

    @Override
    public void initNode() {
        this.setMaxHeight(30);
        this.setMinHeight(30);
        this.setPrefHeight(30);
        this.setId("headerBar");
        NodeAdapter.super.initNode();
    }
}
