package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.ScreenUtil;
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
        return this.getLeading();
    }

    /**
     * 设置内容组件
     *
     * @param content 内容组件
     */
    public void setContent(Node content) {
        this.setLeading(content);
    }

    /**
     * 获取标题组件
     *
     * @return 标题组件
     */
    private FXLabel getTitleLabel() {
        FXPane pane = (FXPane) this.getTrailing();
        if (pane == null || pane.isChildEmpty()) {
            return null;
        }
        return (FXLabel) pane.getFirstChild();
    }

    /**
     * 设置标题组件
     *
     * @param label 标题组件
     */
    private void setTitleLabel(FXLabel label) {
        label.setLayoutX(20);
        label.setLayoutY(6);
        FXPane pane = (FXPane) this.getTrailing();
        if (pane == null) {
            pane = new FXPane();
            pane.setMouseTransparent(true);
        }
        pane.setChild(label);
        this.setTrailing(pane);
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
            // label.setPadding(new Insets(0, 0, 0, 5));
            double w = ScreenUtil.getAllWidth();
            label.setPrefWidth(w);
            label.setMouseTransparent(true);
            label.setFontWeight(FontWeight.BOLD);
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
        label.setText(title);
    }

    // /**
    //  * 初始化中间组件
    //  */
    // private void initCenter() {
    //     FXPane pane = (FXPane) this.getCenter();
    //     if (pane == null) {
    //         pane = new FXPane();
    //         pane.setMouseTransparent(true);
    //         pane.setMinWidth(20);
    //         this.setCenter(pane);
    //     }
    // }

    // @Override
    // public void initNode() {
    //     NodeAdapter.super.initNode();
    //     this.initCenter();
    // }

    // /**
    //  * 原始x
    //  */
    // private Double originalX;
    //
    // /**
    //  * 原始y
    //  */
    // private Double originalY;
    //
    // public void setOriginalX(double originalX) {
    //     this.originalX = originalX;
    // }
    //
    // public Double getOriginalX() {
    //     return this.originalX;
    // }
    //
    // public void setOriginalY(double originalY) {
    //     this.originalY = originalY;
    // }
    //
    // public Double getOriginalY() {
    //     return this.originalY;
    // }
    //
    // /**
    //  * 记录位置
    //  */
    // public void doRecordLocation() {
    //     // 记录原始位置
    //     double[] originalPosition = MouseUtil.getMousePosition();
    //     this.setOriginalX(originalPosition[0]);
    //     this.setOriginalY(originalPosition[1]);
    // }
    //
    // /**
    //  * 清除位置
    //  */
    // public void doClearLocation() {
    //     if (this.originalX != null) {
    //         this.originalX = null;
    //     }
    //     if (this.originalY != null) {
    //         this.originalY = null;
    //     }
    // }
    //
    // /**
    //  * 更新位置
    //  */
    // public void doUpdateLocation() {
    //     try {
    //         Double originalX = this.getOriginalX();
    //         Double originalY = this.getOriginalY();
    //         if (originalX == null || originalY == null) {
    //             return;
    //         }
    //         Window window = this.window();
    //         double[] position = MouseUtil.getMousePosition();
    //         double mouseX = position[0];
    //         double mouseY = position[1];
    //         double nodeX = window.getX();
    //         double nodeY = window.getY();
    //         // 更新x位置
    //         // 计算x差值，不等于0时则更新组件x位置，并更新x值
    //         double x1 = mouseX - this.getOriginalX();
    //         if (x1 != 0) {
    //             window.setX(nodeX + x1);
    //             this.setOriginalX(mouseX);
    //         }
    //         // 更新y位置
    //         // 计算y差值，不等于0时则更新组件x位置，并更新y值
    //         double y1 = mouseY - originalY;
    //         if (y1 != 0) {
    //             window.setY(nodeY + y1);
    //             this.setOriginalY(mouseY);
    //         }
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //     }
    // }
    //
    // /**
    //  * 检查边界
    //  *
    //  * @return 结果
    //  */
    // public boolean checkBounds(MouseEvent event) {
    //     Bounds bounds = this.getBoundsInLocal();
    //     // 仅判断y是否在区间内
    //     return bounds.contains(0, event.getY());
    // }
}
