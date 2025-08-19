package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.node.NodeAdapter;
import javafx.scene.Node;
import javafx.scene.layout.HeaderBar;
import javafx.scene.text.FontWeight;

/**
 * @author oyzh
 * @since 2025-08-19
 */
public class FXHeaderBar extends HeaderBar implements NodeAdapter {

    {
        this.setId("headerBar");
    }

    /**
     * 获取内容
     *
     * @return 内容
     */
    public Node getContent() {
        return this.getLeading();
    }

    /**
     * 设置内容
     *
     * @param content 内容
     */
    public void setContent(Node content) {
        this.setLeading(content);
    }

    /**
     * 获取图标
     *
     * @return 图标
     */
    public Node getIcon() {
        FXLabel label = (FXLabel) this.getTrailing();
        return label == null ? null : label.getGraphic();
    }

    /**
     * 设置图标
     *
     * @param icon 图标
     */
    public void setIcon(Node icon) {
        FXLabel label = (FXLabel) this.getTrailing();
        if (label == null) {
            this.initCenter();
            label = new FXLabel(icon);
            label.setFontWeight(FontWeight.BOLD);
            this.setTrailing(label);
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
        FXLabel label = (FXLabel) this.getTrailing();
        return label == null ? null : label.getText();
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        FXLabel label = (FXLabel) this.getTrailing();
        if (label == null) {
            this.initCenter();
            label = new FXLabel(title);
            label.setFontWeight(FontWeight.BOLD);
            this.setTrailing(label);
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
}
