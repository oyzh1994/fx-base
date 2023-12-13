// package cn.oyzh.fx.plus.tree;
//
// import cn.oyzh.fx.plus.controls.FlexVBox;
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import cn.oyzh.fx.plus.util.NodeUtil;
// import javafx.geometry.Insets;
// import javafx.scene.Node;
// import javafx.scene.control.ScrollBar;
// import javafx.scene.control.Skin;
// import javafx.scene.control.skin.ScrollPaneSkin;
// import javafx.scene.layout.HBox;
// import javafx.scene.paint.Color;
// import lombok.experimental.UtilityClass;
//
// /**
//  * @author oyzh
//  * @since 2023/12/5
//  */
// @UtilityClass
// public class AvdTreeViewUtil extends FlexVBox {
//
//     public static SVGGlyph initRightIcon(AvdTreeItem<?> item) {
//         SVGGlyph glyph = new SVGGlyph("/fx-plus/font/arrow-right-filling.svg");
//         glyph.setSizeStr("12,14");
//         glyph.setColor(Color.GRAY);
//         glyph.setOnMousePrimaryClicked(e -> item.expand());
//         HBox.setMargin(glyph, new Insets(1, 5, 0, 3));
//         return glyph;
//     }
//
//     public static SVGGlyph initDownIcon(AvdTreeItem<?> item) {
//         SVGGlyph glyph = new SVGGlyph("/fx-plus/font/arrow-down-filling.svg");
//         glyph.setSizeStr("14,12");
//         glyph.setColor(Color.GRAY);
//         glyph.setOnMousePrimaryClicked(e -> item.collapse());
//         HBox.setMargin(glyph, new Insets(1, 3, 0, 3));
//         return glyph;
//     }
//
//     public static void updateHeight(AvdTreeItem<?> item) {
//         if (item != null) {
//             double size = 0;
//             for (Node child : item.getChildren()) {
//                 if (child.isVisible() && child.isManaged()) {
//                     size += NodeUtil.getHeight(child);
//                 }
//             }
//             NodeUtil.setHeight(item, size);
//             if (item.getItemParent() != null) {
//                 updateHeight(item.getItemParent());
//             } else {
// //                double size1 = 0;
// //                for (Node child : item.getTreeView().content().getChildren()) {
// //                    if (child.isVisible() && child.isManaged()) {
// //                        size1 += NodeUtil.getHeight(child);
// //                    }
// //                }
// //                // NodeUtil.setHeight(item.getTreeView(), size1);
// //                NodeUtil.setHeight(item.getTreeView().getContent(), size1);
// //
// ////                item.getTreeView().setHmax(size1);
// ////                item.getTreeView().setHmin(size1);
// ////                item.getTreeView().setVmax(size1);
// ////                item.getTreeView().setVmin(size1);
// ////                item.getTreeView().setPrefViewportHeight(size1);
// //                item.getTreeView().setMinViewportHeight(size1);
// //                item.getTreeView().setPrefViewportHeight(size1);
// //                item.getTreeView().setMinViewportWidth(size1);
// //                item.getTreeView().setPrefViewportWidth(size1);
// ////                item.getTreeView().setVvalue(size1);
// ////                item.getTreeView().setHvalue(size1);
// //
// ////
// ////                ScrollPaneSkin skin = (ScrollPaneSkin) item.getTreeView().getSkin();
// ////                ScrollBar vbar = skin.getVerticalScrollBar();
// ////                vbar.setMax(size1);
// ////                vbar.setMin(size1);
// ////                vbar.setVisibleAmount(size1);
// ////                vbar.adjustValue(size1);
// //
// ////                System.out.println(skin.getVerticalScrollBar());
// ////                System.out.println(skin.getVerticalScrollBar().getMax());
// //
// //
// //                System.out.println("---------size1=" + size1);
// //                System.out.println("---------ViewportHeight1=" + item.getTreeView().getMinViewportHeight());
// //                System.out.println("---------ViewportHeight2=" + item.getTreeView().getPrefViewportHeight());
// ////                System.out.println("---------size2=" + item.getTreeView().content().getMinHeight());
// ////                System.out.println("---------size2=" + item.getTreeView().content().getHeight());
// ////                System.out.println("---------size2=" + item.getTreeView().getHeight());
//             }
//         }
//     }
// }
