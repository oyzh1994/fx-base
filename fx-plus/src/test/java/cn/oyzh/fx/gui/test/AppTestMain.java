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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Pagination;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
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
        // test3(stage);
        //test4(stage);
//        test5(stage);
        test6(stage);
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

    private void test4(Stage stage) {
        // 创建一个轻量级HBox
        VBox vbox = new VBox();

        Pagination pagination = new Pagination();
        pagination.setPageCount(10);
        pagination.setMaxPageIndicatorCount(3);
        pagination.setCurrentPageIndex(3);

        vbox.getChildren().addAll(pagination);

        Slider slider = new Slider();
        vbox.getChildren().addAll(slider);

        Spinner<Integer> spinner = new Spinner<>();
        spinner.setEditable(true);
        spinner.setValueFactory(new SpinnerValueFactory<>() {
            @Override
            public void decrement(int steps) {

            }

            @Override
            public void increment(int steps) {

            }
        });

        vbox.getChildren().addAll(spinner);


        SplitMenuButton button = new SplitMenuButton();
        SplitPane button1 = new SplitPane();

        vbox.getChildren().addAll(button);
        // vbox.getChildren().addAll(button1);

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(new Button("aaa"));
        toolBar.getItems().addAll(new Button("aaa"));
        toolBar.getItems().addAll(new OpenSVGGlyph());
        vbox.getChildren().addAll(toolBar);


        LabeledComboBox comboBox = new LabeledComboBox("test");
        comboBox.setItems(List.of("a", "b", "c"));
        comboBox.setKey("test1");
        vbox.getChildren().add(comboBox);

        LabeledComboBox1 comboBox1 = new LabeledComboBox1("test");
        vbox.getChildren().add(comboBox1);

        Scene scene = new Scene(vbox, 400, 300);
        stage.setTitle("titledPane测试");
        stage.setScene(scene);
        stage.show();

        comboBox.update_view();

    }

    private void test5(Stage stage) {
        // 创建一个轻量级HBox
        VBox vbox = new VBox();

        //LabeledComboBox comboBox = new LabeledComboBox("test");
        //comboBox.setItems(List.of("a", "b", "c"));
        //comboBox.setKey("test1");
        //vbox.getChildren().add(comboBox);

        LabeledComboBox1 comboBox1 = new LabeledComboBox1("test");
        vbox.getChildren().add(comboBox1);

        Scene scene = new Scene(vbox, 400, 300);
        stage.setTitle("titledPane测试");
        stage.setScene(scene);
        stage.show();

    }

    private void test6(Stage stage) {
        // 创建一个轻量级HBox
        VBox vbox = new VBox();


        // 原始数据
        ObservableList<String> originalData = FXCollections.observableArrayList(
                "Apple", "Banana", "Orange", "Grape", "Mango", "Peach", "Pear"
        );

        // 创建一个FilteredList，初始时显示所有数据
        FilteredList<String> filteredData = new FilteredList<>(originalData, p -> true);
        ComboBox<String> comboBox1 = new ComboBox<>();
        comboBox1.setItems(filteredData);
        comboBox1.setEditable(true);
        comboBox1.show();
        vbox.getChildren().add(comboBox1);


        ChoiceBox<String> choiceBox1 = new ChoiceBox<>();
        choiceBox1.setItems(FXCollections.observableArrayList(List.of("test3", "test4")));
        vbox.getChildren().add(choiceBox1);

        Scene scene = new Scene(vbox, 400, 300);
        stage.setTitle("titledPane测试");
        stage.setScene(scene);
        stage.show();
        comboBox1.getEditor().textProperty().addListener((observableValue, s, newValue) -> {
            System.out.println(newValue);
            // 设置过滤条件：包含新输入的文本（不区分大小写）
            filteredData.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // 如果输入为空，显示所有
                }
                // 检查项目是否包含输入的字符串（忽略大小写）
                return item.toLowerCase().contains(newValue.toLowerCase());
            });
            comboBox1.show();
        });


        // 当选择项发生变化时，确保编辑器中的文本是选中的项（可选）
        comboBox1.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                comboBox1.getEditor().setText(newValue);
            }
        });

    }

    public static class AppTestMainApp {

        public static void main(String[] args) {
            AppTestMain.main(args);
        }
    }
}
