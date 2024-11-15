package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.gui.svg.glyph.AddSVGGlyph;
import cn.oyzh.fx.plus.gui.svg.glyph.ChooseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class TreeViewMain extends Application {

    public static void main(String[] args) {
        launch(TreeViewMain.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // test2(stage);
        // test3(stage);
        // test4(stage);
        test5(stage);
        stage.setTitle("JavaFX Demo");
    }

    private void test2(Stage stage) {
        TreeView<String> treeView = new TreeView<>();
        TreeItem<String> root = new TreeItem<>("11");
        ChooseSVGGlyph glyph = new ChooseSVGGlyph();
        glyph.setColor(Color.BLACK);
        root.setGraphic(glyph);
        treeView.setRoot(root);

        SVGPath svgPath = new SVGPath();
        svgPath.setContent("M0 465.454545l1024 0 0 93.090909-1024 0 0-93.090909Z M465.454545 0l93.090909 0 0 1024-93.090909 0 0-1024Z");
        svgPath.setFill(Color.BLACK);

        for (int i = 0; i < 100_000; i++) {
            TreeItem<String> treeItem = new TreeItem<>("22");
            // ChooseSVGGlyph glyph1 = new ChooseSVGGlyph();
            // glyph1.setColor(Color.BLACK);


            // svgPath.setStrokeWidth(10);

            Region region = new Region();
            region.setMaxWidth(20);
            region.setMinHeight(20);
            region.setPrefHeight(20);
            region.setPrefWidth(20);
            region.setStyle("-fx-background-color: #ccc");

            region.setShape(svgPath);

            treeItem.setGraphic(region);

            root.getChildren().add(treeItem);
        }
        root.setExpanded(true);

        stage.setScene(new Scene(treeView, 500, 500));
        stage.show();
    }

    private void test3(Stage stage) {
        TreeView<String> treeView = new TreeView<>();
        TreeItem<String> root = new TreeItem<>("11");
        ChooseSVGGlyph glyph = new ChooseSVGGlyph();
        glyph.setColor(Color.BLACK);
        root.setGraphic(glyph);
        treeView.setRoot(root);
        for (int i = 0; i < 100_000; i++) {
            TreeItem<String> treeItem = new TreeItem<>("22");
            AddSVGGlyph glyph1 = new AddSVGGlyph();
            // glyph1.setStyle("-fx-background-color: #ccc");
            treeItem.setGraphic(glyph1);
            root.getChildren().add(treeItem);
        }
        root.setExpanded(true);

        stage.setScene(new Scene(treeView, 500, 500));
        stage.show();
    }

    private void test4(Stage stage) {
        TreeView<String> treeView = new TreeView<>();
        TreeItem<String> root = new TreeItem<>("11");
        ChooseSVGGlyph glyph = new ChooseSVGGlyph();
        glyph.setColor(Color.BLACK);
        root.setGraphic(glyph);
        treeView.setRoot(root);
        for (int i = 0; i < 100_000; i++) {
            SVGPath svgPath = SVGManager.load("/fx-plus/font/add.svg");
            TreeItem<String> treeItem = new TreeItem<>("22");
            Region region = new Region();
            region.setMaxWidth(20);
            region.setMinHeight(20);
            region.setPrefHeight(20);
            region.setPrefWidth(20);
            region.setShape(svgPath);
            region.setStyle("-fx-background-color: #ccc");
            treeItem.setGraphic(region);
            root.getChildren().add(treeItem);
        }
        root.setExpanded(true);

        stage.setScene(new Scene(treeView, 500, 500));
        stage.show();
    }

    private void test5(Stage stage) {
        TreeView<String> treeView = new TreeView<>();
        // ChooseSVGGlyph glyph = new ChooseSVGGlyph();

        // SVGPath icon = SVGLoader.INSTANCE.load("/fx-plus/font/check.svg");
        //
        // Region region = new Region();
        // region.setMaxWidth(20);
        // region.setMinHeight(20);
        // region.setPrefHeight(20);
        // region.setShape(icon);

        TreeItem<String> root = new TreeItem<>("root");
        root.setGraphic(new SVGGlyph("/fx-plus/font/check.svg"));
        // ChooseSVGGlyph glyph = new ChooseSVGGlyph();
        // glyph.setColor(Color.BLACK);
        // root.setGraphic(glyph);
        treeView.setRoot(root);
        for (int i = 0; i < 10000; i++) {
            // SVGPath svgPath = SVGManager.load("/fx-plus/font/add.svg");
            TreeItem<String> treeItem = new TreeItem<>("sub" + i);
            treeItem.setGraphic(new SVGGlyph("/fx-plus/font/check.svg"));
            // Region region = new Region();
            // region.setMaxWidth(20);
            // region.setMinHeight(20);
            // region.setPrefHeight(20);
            // region.setPrefWidth(20);
            // region.setShape(svgPath);
            // region.setStyle("-fx-background-color: #ccc");
            // treeItem.setGraphic(region);
            root.getChildren().add(treeItem);
        }
        root.setExpanded(true);

        stage.setScene(new Scene(treeView, 500, 500));
        stage.show();
    }

}
