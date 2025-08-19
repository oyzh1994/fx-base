package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.controls.label.FXLabel;
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
        return this.getTrailing();
    }

    /**
     * 设置内容
     *
     * @param content 内容
     */
    public void setContent(Node content) {
        this.setTrailing(content);
    }

    /**
     * 获取图标
     *
     * @return 图标
     */
    public Node getIcon() {
        FXLabel label = (FXLabel) this.getLeading();
        return label == null ? null : label.getGraphic();
    }

    /**
     * 设置图标
     *
     * @param icon 图标
     */
    public void setIcon(Node icon) {
        FXLabel label = (FXLabel) this.getLeading();
        if (label == null) {
            label = new FXLabel();
            label.setFontWeight(FontWeight.BOLD);
            label.setGraphic(icon);
            this.setLeading(label);
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
        FXLabel label = (FXLabel) this.getLeading();
        return label == null ? null : label.getText();
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        FXLabel label = (FXLabel) this.getLeading();
        if (label == null) {
            label = new FXLabel(title);
            label.setFontWeight(FontWeight.BOLD);
            this.setCenter(label);
        } else {
            label.setText(title);
        }
    }
}
