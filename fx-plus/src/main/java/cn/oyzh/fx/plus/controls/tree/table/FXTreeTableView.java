// package cn.oyzh.fx.plus.controls.tree.table;
//
// import cn.oyzh.common.log.JulLog;
// import cn.oyzh.common.object.Destroyable;
// import cn.oyzh.common.thread.TaskManager;
// import cn.oyzh.fx.plus.adapter.DestroyAdapter;
// import cn.oyzh.fx.plus.adapter.SelectAdapter;
// import cn.oyzh.fx.plus.adapter.StateAdapter;
// import cn.oyzh.fx.plus.flex.FlexAdapter;
// import cn.oyzh.fx.plus.flex.FlexUtil;
// import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
// import cn.oyzh.fx.plus.mouse.MouseAdapter;
// import cn.oyzh.fx.plus.node.NodeAdapter;
// import cn.oyzh.fx.plus.node.NodeDisposeUtil;
// import cn.oyzh.fx.plus.node.NodeManager;
// import cn.oyzh.fx.plus.node.NodeUtil;
// import cn.oyzh.fx.plus.theme.ThemeAdapter;
// import cn.oyzh.fx.plus.theme.ThemeStyle;
// import cn.oyzh.fx.plus.util.FXUtil;
// import javafx.collections.ObservableList;
// import javafx.scene.control.TreeItem;
// import javafx.scene.control.TreeTableColumn;
// import javafx.scene.control.TreeTableView;
//
// import java.util.function.Consumer;
//
// /**
//  * 树形结构
//  *
//  * @author oyzh
//  * @since 2022/1/19
//  */
// public class FXTreeTableView extends TreeTableView implements FlexAdapter, DestroyAdapter, NodeAdapter, ThemeAdapter, ContextMenuAdapter, MouseAdapter, SelectAdapter<TreeItem<?>>, StateAdapter {
//
//     {
//         NodeManager.init(this);
//     }
//
//     /**
//      * 重新选择节点
//      */
//     public void reselect() {
//         TreeItem<?> item = this.getSelectedItem();
//         if (item == null) {
//             this.select(null);
//         } else {
//             this.clearSelection();
//             this.select(item);
//         }
//     }
//
//     /**
//      * 节点变更事件
//      *
//      * @param consumer 消费器
//      */
//     public void selectItemChanged( Consumer<TreeItem<?>> consumer) {
//         this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//             if (!this.isIgnoreChanged()) {
//                 consumer.accept((TreeItem<?>) newValue);
//             }
//         });
//     }
//
//     @Override
//     public void scrollTo(int index) {
//         FXUtil.runWait(() -> super.scrollTo(index));
//     }
//
//     /**
//      * 滚动到目标节点
//      *
//      * @param item 节点
//      */
//     public void scrollTo(TreeItem<?> item) {
//         if (item != null) {
//             int index = this.getRow(item);
//             if (index >= 0) {
//                 this.scrollTo(index);
//             } else {
//                 JulLog.warn("row index:{} invalid.", index);
//             }
//         }
//     }
//
//     /**
//      * 选中并滚动到节点
//      *
//      * @param item 节点
//      */
//     public void selectAndScroll(TreeItem<?> item) {
//         this.select(item);
//         this.scrollTo(item);
//     }
//
//     /**
//      * 节点是否选中
//      *
//      * @param item 节点
//      * @return 结果
//      */
//     public boolean isSelected(TreeItem<?> item) {
//         return item != null && this.getSelectedItem() == item;
//     }
//
//     /**
//      * 刷新坐标，防止出现白屏
//      */
//     public void flushLocal() {
//         TaskManager.startDelay("tree:flushLocal:" + this.hashCode(), () -> FXUtil.runLater(() -> {
//             this.layoutChildren();
//             this.localToScreen(this.getBoundsInLocal());
//             this.refresh();
//         }), 100);
//     }
//
//     @Override
//     public void changeTheme(ThemeStyle style) {
//         ThemeAdapter.super.changeTheme(style);
//         this.refresh();
//     }
//
//     @Override
//     public void destroy() {
//         if (this.getRoot() instanceof Destroyable destroyable) {
//             destroyable.destroy();
//             this.setRoot(null);
//         }
//     }
//
//     /**
//      * 定位节点
//      */
//     public void positionItem() {
//         this.scrollTo(this.getSelectedItem());
//     }
//
//     @Override
//     public void resize(double width, double height) {
//         double[] size = this.computeSize(width, height);
//         super.resize(size[0], size[1]);
//         this.resizeNode();
//     }
//
//     @Override
//     public void resizeNode(Double width, Double height) {
//         // 调用父类的resizeNode方法来调整节点大小
//         FlexAdapter.super.resizeNode(width, height);
//         // 获取表格中的列
//         ObservableList<? extends TreeTableColumn<?, ?>> columns = this.getColumns();
//         // 遍历每一列
//         for (TreeTableColumn<?, ?> column : columns) {
//             // 判断列是否是FlexAdapter的实例
//             if (column instanceof FlexAdapter flexNode) {
//                 // 如果列可见，则设置实际宽度为计算得到的弹性宽度
//                 if (column.isVisible()) {
//                     flexNode.setRealWidth(FlexUtil.compute(flexNode.getFlexWidth(), width));
//                 } else {
//                     // 否则将列宽度设置为0
//                     NodeUtil.setWidth(column, 0D);
//                 }
//             }
//         }
//     }
// }
