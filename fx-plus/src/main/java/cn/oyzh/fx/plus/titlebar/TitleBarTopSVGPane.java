// package cn.oyzh.fx.plus.titlebar;
//
// import cn.oyzh.fx.plus.adapter.StateAdapter;
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import cn.oyzh.fx.plus.mouse.MouseAdapter;
// import javafx.scene.layout.HBox;
// import lombok.Getter;
// import lombok.Setter;
//
// /**
//  * @author oyzh
//  * @since 2024-12-09
//  */
// public class TitleBarTopSVGPane extends HBox implements MouseAdapter, StateAdapter {
//
//     @Getter
//     @Setter
//     private String size;
//
//     public TitleBarTopSVGPane(String size) {
//         this.size = size;
//         this.top();
//     }
//
//     public void unTop() {
//         this.getChildren().setAll(new TitleBarUnTopSVGGlyph(this.size));
//     }
//
//     public void top() {
//         this.getChildren().setAll(new TitleBarTopSVGGlyph(this.size));
//     }
//
//     public boolean isTop() {
//         SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
//         return svgGlyph.getUrl().contains("un-maximum.svg");
//     }
//
//     public void setTop(boolean top) {
//         if (top) {
//             this.top();
//         } else {
//             this.unTop();
//         }
//     }
// }
