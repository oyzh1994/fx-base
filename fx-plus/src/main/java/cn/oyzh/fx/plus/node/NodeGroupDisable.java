// package cn.oyzh.fx.plus.node;
//
// import javafx.scene.Node;
//
// /**
//  * 节点分组禁用
//  *
//  * @author oyzh
//  * @since 2023/08/09
//  */
// public class NodeGroupDisable extends NodeGroupAble {
//
//     /**
//      * 是否禁用
//      */
//     private Boolean disabled;
//
//     /**
//      * 执行禁用
//      */
//     public void disable() {
//         this.disabled = true;
//         for (Node node : this.nodes) {
//             node.setDisable(this.disabled);
//         }
//     }
//
//     /**
//      * 执行启用
//      */
//     public void enable() {
//         this.disabled = false;
//         for (Node node : this.nodes) {
//             node.setDisable(this.disabled);
//         }
//     }
//
//     @Override
//     protected void onAdd(Node node) {
//         if (this.disabled != null) {
//             node.setDisable(this.disabled);
//         }
//     }
// }
