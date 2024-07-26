// package cn.oyzh.fx.plus.window;
//
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import cn.oyzh.fx.plus.util.AnimationUtil;
// import cn.oyzh.fx.plus.util.FXUtil;
// import javafx.animation.RotateTransition;
// import javafx.stage.Stage;
// import javafx.stage.StageStyle;
//
// /**
//  * @author oyzh
//  * @since 2024/07/26
//  */
// public class LoadingStage extends Stage implements StageAdapter {
//
//     private SVGGlyph loading;
//
//     private RotateTransition timer;
//
//     public LoadingStage() {
//         this.initStyle(StageStyle.UNDECORATED);
//         this.setWidth(150);
//         this.setHeight(150);
//     }
//
//     @Override
//     public Stage stage() {
//         return this;
//     }
//
//     @Override
//     public void display() {
//         if (!this.isShowing()) {
//             if (this.loading == null) {
//                 this.loading = new SVGGlyph("loading");
//                 this.timer = AnimationUtil.rotate(this.loading);
//                 this.root(this.loading);
//                 this.onShownProperty().addListener((observable, oldValue, newValue) -> this.timer.play());
//                 this.onHiddenProperty().addListener((observable, oldValue, newValue) -> this.timer.stop());
//             }
//             super.show();
//         }
//     }
//
//     // @Override
//     // public void hide() {
//     //     try {
//     //         super.hide();
//     //         if (this.timer != null) {
//     //             this.timer.stop();
//     //         }
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //     }
//     // }
// }
