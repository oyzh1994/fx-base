//package cn.oyzh.fx.plus.information;
//
//import cn.oyzh.fx.common.thread.ExecutorUtil;
//import cn.oyzh.fx.plus.util.ControlUtil;
//import cn.oyzh.fx.plus.util.FXUtil;
//import cn.oyzh.fx.plus.util.FontUtil;
//import javafx.event.EventHandler;
//import javafx.scene.Node;
//import javafx.scene.control.Tooltip;
//import javafx.scene.text.Font;
//import javafx.stage.WindowEvent;
//import javafx.util.Duration;
//import lombok.Getter;
//import lombok.NonNull;
//import lombok.Setter;
//import lombok.experimental.Accessors;
//
//import java.awt.*;
//
///**
// * 提示窗口
// * 适合输入框，文本框提示等
// *
// * @author oyzh
// * @since 2022/2/8
// */
//@Deprecated
//public class FXTip {
//
//    /**
//     * 待提示的节点
//     */
//    private Node tipNode;
//
//    /**
//     * 提示消息
//     */
//    private String tipMsg;
//
//    /**
//     * 提示组件
//     */
//    private Tooltip tooltip;
//
//    /**
//     * 提示距离控件最左边的距离比
//     */
//    @Getter
//    @Setter
//    @Accessors(chain = true, fluent = true)
//    private double leftPercent = 0.85;
//
//    /**
//     * 存活时间
//     */
//    @Setter
//    @Getter
//    private Duration liveTime = new Duration(1500);
//
//    public FXTip(@NonNull Node tipNode, @NonNull String tipMsg) {
//        this.tipMsg = tipMsg;
//        this.tipNode = tipNode;
//        this.tooltip = new Tooltip();
//        ControlUtil.initTooltip(this.tooltip);
//    }
//
//    /**
//     * 显示
//     */
//    public void show() {
//        // 计算x，y的位置
//        double absX = FXUtil.getAbsoluteX(this.tipNode) + this.tipNode.prefWidth(0) * this.leftPercent;
//        double abxY = FXUtil.getAbsoluteY(this.tipNode);
//        double height = this.tipNode.prefHeight(0);
//        int fontSize = 24;
//        do {
//            FontMetrics fontMetrics = FontUtil.fontMetrics(this.tooltip.getFont());
//            if (fontMetrics.getHeight() > height) {
//                this.tooltip.setPrefHeight(height);
//                this.tooltip.setFont(new Font(fontSize));
//                break;
//            }
//        } while (fontSize-- > 10);
//        // 设置信息
//        this.tooltip.setText(this.tipMsg);
//        // 自动隐藏
//        if (this.liveTime != null) {
//            this.tooltip.setOnShown(e -> ExecutorUtil.start(this::close, (int) this.liveTime.toMillis()));
//        }
//        // 显示组件
//        FXUtil.runLater(() -> {
//            this.tooltip.show(this.tipNode, absX, abxY);
//            this.tipNode.requestFocus();
//        });
//    }
//
//    /**
//     * 关闭
//     */
//    public void close() {
//        try {
//            if (this.tooltip != null) {
//                FXUtil.runLater(this.tooltip::hide);
//            }
//            this.tipMsg = null;
//            this.tipNode = null;
//            this.tooltip = null;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    /**
//     * 设置隐藏事件处理器
//     *
//     * @param handler 处理器
//     */
//    public void setOnHidden(EventHandler<WindowEvent> handler) {
//        this.tooltip.setOnHidden(handler);
//    }
//
//    /**
//     * 是否现实中
//     *
//     * @return 结果
//     */
//    public boolean isShowing() {
//        return this.tooltip.isShowing();
//    }
//}
