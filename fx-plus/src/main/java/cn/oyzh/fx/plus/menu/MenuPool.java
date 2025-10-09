// package cn.oyzh.fx.plus.menu;
//
// import cn.oyzh.common.util.Pool;
// import javafx.scene.control.Menu;
//
// /**
//  * @author oyzh
//  * @since 2025-06-25
//  */
// public class MenuPool extends Pool<Menu> {
//
//     public MenuPool() {
//         super(1, 10);
//     }
//
//     @Override
//     protected Menu newObject() {
//         return new FXMenu();
//     }
//
//     @Override
//     public void returnObject(Menu item) {
//         if (item == null) {
//             return;
//         }
//         // 尽可能清除属性
//         item.setId(null);
//         item.setText(null);
//         item.getItems().clear();
//         item.setStyle(null);
//         item.setGraphic(null);
//         item.setOnAction(null);
//         item.setVisible(true);
//         item.setDisable(false);
//         item.setUserData(null);
//         item.setAccelerator(null);
//         item.getProperties().clear();
//         item.disableProperty().unbind();
//         item.setOnMenuValidation(null);
//         super.returnObject(item);
//     }
// }
