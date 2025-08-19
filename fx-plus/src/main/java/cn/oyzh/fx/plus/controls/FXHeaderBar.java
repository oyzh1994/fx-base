package cn.oyzh.fx.plus.controls;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import javafx.scene.Node;
import javafx.scene.layout.HeaderBar;
import javafx.scene.text.FontWeight;

/**
 * @author oyzh
 * @since 2025-08-19
 */
public class FXHeaderBar extends HeaderBar implements NodeAdapter {

    {
        NodeManager.init(this);
        this.setId("headerBar");
    }

    /**
     * 获取内容组件
     *
     * @return 内容组件
     */
    public Node getContent() {
        if (OSUtil.isWindows()) {
            return this.getTrailing();
        }
        return this.getLeading();
    }

    /**
     * 设置内容组件
     *
     * @param content 内容组件
     */
    public void setContent(Node content) {
        if (OSUtil.isWindows()) {
            this.setTrailing(content);
        } else {
            this.setLeading(content);
        }
    }

    /**
     * 获取标题组件
     *
     * @return 标题组件
     */
    private FXLabel getTitleLabel() {
        if (OSUtil.isWindows()) {
            return (FXLabel) this.getLeading();
        }
        return (FXLabel) this.getTrailing();
    }

    /**
     * 设置标题组件
     *
     * @param label 标题组件
     */
    private void setTitleLabel(FXLabel label) {
        if (OSUtil.isWindows()) {
            this.setLeading(label);
        } else {
            this.setTrailing(label);
        }
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
        FXLabel label = this.getTitleLabel();
        if (label == null) {
            label = new FXLabel(icon);
            label.setFontWeight(FontWeight.BOLD);
            this.setTitleLabel(label);
        } else {
            label.setGraphic(icon);
        }
    }

    /**
     * 获取标题
     *
     * @return 标题
     */
    public String getTitle() {
        this.initCenter();
        FXLabel label = this.getTitleLabel();
        return label == null ? null : label.getText();
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        FXLabel label = this.getTitleLabel();
        if (label == null) {
            label = new FXLabel(title);
            label.setFontWeight(FontWeight.BOLD);
            this.setTitleLabel(label);
        } else {
            label.setText(title);
        }
    }

    /**
     * 初始化中间组件
     */
    private void initCenter() {
        FXPane pane = (FXPane) this.getCenter();
        if (pane == null) {
            pane = new FXPane();
            pane.setMinWidth(20);
            this.setCenter(pane);
        }
    }

    @Override
    public void initNode() {
        NodeAdapter.super.initNode();
        this.initCenter();
    }
}
