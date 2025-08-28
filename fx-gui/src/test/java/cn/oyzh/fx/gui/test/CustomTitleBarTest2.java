package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.controls.FXHeaderBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.util.Set;

/**
 * @author oyzh
 * @since 2025-08-18
 */
public class CustomTitleBarTest2 extends Application {

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        // System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // ThemeManager.apply(Themes.PRIMER_LIGHT);
        // ThemeManager.apply(Themes.PRIMER_DARK);
        // Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
        FXHeaderBar headerBar = new FXHeaderBar();
        Button button1 = new Button("test1");
        Button button2 = new Button("test2");
        Button button3 = new Button("test3");
        Button button4 = new Button("test4");
        Button button5 = new Button("test5");
        Button button6 = new Button("test6");
        Button button7 = new Button("test7");
        Button button8 = new Button("test8");
        Button button9 = new Button("test9");
        FXPane hBox = new FXPane();
        hBox.addChild(button1);
        // hBox.addChild(button2);
        // hBox.addChild(button3);
        // hBox.addChild(button4);
        // hBox.addChild(button5);
        // hBox.addChild(button6);
        // hBox.addChild(button7);
        // hBox.addChild(button8);
        // hBox.addChild(button9);
        headerBar.setContent(hBox);
        // headerBar.setTitle("测试标题");
        headerBar.setStyle("-fx-background-color: red;-fx-text-fill: red;-fx-fill: red");

        System.out.println(headerBar.getStyle());
        Set<Node> nodes = headerBar.lookupAll("*");
        for (Node node : nodes) {
            System.out.println(node);
        }
        VBox root = new VBox();
        root.getChildren().add(headerBar);
        root.getChildren().add(new FXLabel("1111"));
        RichTreeView treeView = new RichTreeView() {
            @Override
            protected void initTreeView() {
                this.dragContent = "test_connect_tree_drag";
                this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
                super.initTreeView();
            }
        };

        RichTreeItem rootItem = new RichTreeItem(treeView) {

        };
        RichTreeItemValue rootValue = new RichTreeItemValue() {
            public String name() {
                return "根节点";
            }
        };
        rootItem.setValue(rootValue);

        for (int i = 0; i < 3; i++) {
            int finalI = i;
            ParentTreeItem pItem1 = new ParentTreeItem (treeView) ;
            RichTreeItemValue pValue1 = new RichTreeItemValue() {
                public String name() {
                    return "父节点" + finalI;
                }
            };
            pItem1.setValue(pValue1);
            rootItem.addChild(pItem1);
            for (int j = 0; j <10 ; j++) {
                int finalJ = j;
                SubTreeItem sItem1 = new SubTreeItem (treeView) ;
                RichTreeItemValue sValue1 = new RichTreeItemValue() {
                    public String name() {
                        return "子节点" + finalJ;
                    }
                };
                sItem1.setValue(sValue1);
                pItem1.addChild(sItem1);
            }
        }

        treeView.setRoot(rootItem);
        root.getChildren().add(treeView);

        Scene scene = new Scene(root, (double) 800.0F, (double) 600.0F);
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.EXTENDED);
        stage.setScene(scene);
        stage.show();

        stage.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            System.out.println(event.getTarget());
        });
    }

    public static class CustomTitleBar2Starter {
        public static void main(String[] args) {
            CustomTitleBarTest2.main(args);
        }
    }
}
