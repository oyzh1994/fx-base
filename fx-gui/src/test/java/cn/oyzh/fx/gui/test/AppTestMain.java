package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.gui.svg.glyph.CloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.OpenSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SaveSVGGlyph;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.DigitalTextField;
import cn.oyzh.fx.gui.text.field.LimitTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.converter.DigitalConverter;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.UnaryOperator;

/**
 * @author oyzh
 * @since 2025-06-12
 */
public class AppTestMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // test1(stage);
        // test2(stage);
        test3(stage);
    }

    private void test1(Stage stage) {
        // 创建一个轻量级HBox
        VBox vbox = new VBox();
        NumberTextField textField = new NumberTextField();
        textField.setMaxWidth(200);
        textField.setMin(10L);
        textField.setMax(20L);
        vbox.getChildren().addAll(textField);

        DecimalTextField decimalTextField = new DecimalTextField();
        decimalTextField.setMaxWidth(200);
        decimalTextField.setScaleLen(2);
        decimalTextField.setMin(10d);
        decimalTextField.setMax(20d);
        vbox.getChildren().addAll(decimalTextField);

        TextField textField1 = new TextField();
        textField1.setMaxWidth(200);
        vbox.getChildren().addAll(textField1);

        FXTextField textField2 = new FXTextField();
        textField2.addTextChangeListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println(newValue);
            }
        });
        textField2.setMaxWidth(200);
        vbox.getChildren().addAll(textField2);

        DigitalTextField textField3 = new DigitalTextField(false, 10L) {
            @Override
            protected DigitalConverter getConverter() {
                return null;
            }

            @Override
            protected void incrValue() {

            }

            @Override
            protected void decrValue() {

            }

            @Override
            protected UnaryOperator<TextFormatter.Change> createFilter() {
                return null;
            }
        };
        textField3.setMaxWidth(200);
        vbox.getChildren().addAll(textField3);

        LimitTextField textField4 = new LimitTextField();
        textField4.setMaxWidth(200);
        vbox.getChildren().addAll(textField4);

        Scene scene = new Scene(vbox, 400, 300);
        stage.setTitle("轻量级布局容器示例");
        stage.setScene(scene);
        stage.show();
    }

    private void test2(Stage stage) {
        // 创建一个轻量级HBox
        VBox vbox = new VBox();

        MenuBar menuBar = new MenuBar();
        Menu m1 = new Menu("11");
        m1.setGraphic(new SaveSVGGlyph());
        menuBar.getMenus().add(m1);
        vbox.getChildren().addAll(menuBar);

        Scene scene = new Scene(vbox, 400, 300);
        stage.setTitle("菜单测试");
        stage.setScene(scene);
        stage.show();
    }

    private void test3(Stage stage) {
        // 创建一个轻量级HBox
        VBox vbox = new VBox();

        Accordion accordion = new Accordion();
        TitledPane titledPane1 = new TitledPane("测试1", new SaveSVGGlyph());
        TitledPane titledPane2 = new TitledPane("测试2", new OpenSVGGlyph());
        TitledPane titledPane3 = new TitledPane("测试3", new CloseSVGGlyph());
        accordion.getPanes().addAll(titledPane1, titledPane2, titledPane3);
        accordion.setExpandedPane(titledPane1);
        vbox.getChildren().addAll(accordion);

        Scene scene = new Scene(vbox, 400, 300);
        stage.setTitle("titledPane测试");
        stage.setScene(scene);
        stage.show();
    }

    public static class AppTestMainApp {

        public static void main(String[] args) {
            AppTestMain.main(args);
        }
    }
}
