package cn.oyzh.fx.plus.test;

import atlantafx.base.controls.Message;
import atlantafx.base.controls.Notification;
import atlantafx.base.util.Animations;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.common.thread.ThreadUtil;
import cn.oyzh.fx.plus.controls.area.MsgTextArea;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.digital.DecimalTextField;
import cn.oyzh.fx.plus.controls.digital.NumberTextField;
import cn.oyzh.fx.plus.controls.popup.SearchHistoryPopup;
import cn.oyzh.fx.plus.controls.search.SearchTextField;
import cn.oyzh.fx.plus.controls.select.SelectTextFiled;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPathExt;
import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.stage.StageManager;
import cn.oyzh.fx.plus.stage.StageWrapper;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.trees.RichTreeItem;
import cn.oyzh.fx.plus.trees.RichTreeItemValue;
import cn.oyzh.fx.plus.trees.RichTreeView;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.TextUtil;
import cn.oyzh.fx.plus.value.DoubleTextValueFactory;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class AppMain extends Application {

    public static void main(String[] args) {
        launch(AppMain.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // test1(stage);
        // test2(stage);
        // test3(stage);
        // test4(stage);
        // test5(stage);
//        test6(stage);
//        test7(stage);
//         test8(stage);
//         test9(stage);
        // test10(stage);
        // test11(stage);
        // test12(stage);
        // test13(stage);
        // test14(stage);
        // test15(stage);
        // test16(stage);
        // test17(stage);
        // test18(stage);
        // test19(stage);
        // test20(stage);
        // test21(stage);
        // test22(stage);
        // test23(stage);
//        test24(stage);
//         test25(stage);
        test26(stage);
    }

    private void test1(Stage stage) {
        TextArea area = new TextArea();
        UnaryOperator<TextFormatter.Change> operator = new UnaryOperator<TextFormatter.Change>() {
            @Override
            public TextFormatter.Change apply(TextFormatter.Change change) {
                System.out.println(change);

                return change;
            }
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(operator);
        area.setTextFormatter(textFormatter);
        HBox group = new HBox(area);
        stage.setScene(new Scene(group, 500, 500));
        stage.show();
    }

    // private void test2(Stage stage) {
    //     TextArea area = new TextArea();
    //     NumberFormatterParam param = new NumberFormatterParam();
    //     param.setMaxVal(100L);
    //     param.setMinVal(1L);
    //     NumberTextFormatter textFormatter = new NumberTextFormatter(param);
    //     area.setTextFormatter(textFormatter);
    //     HBox group = new HBox(area);
    //     stage.setScene(new Scene(group, 500, 500));
    //     stage.show();
    // }
    //
    // private void test3(Stage stage) {
    //     TextArea area = new TextArea();
    //     DecimalFormatterParam param = new DecimalFormatterParam();
    //     param.setMaxVal(100.1d);
    //     param.setMinVal(1.2d);
    //     DecimalTextFormatter textFormatter = new DecimalTextFormatter(param);
    //     area.setTextFormatter(textFormatter);
    //     HBox group = new HBox(area);
    //     stage.setScene(new Scene(group, 500, 500));
    //     stage.show();
    // }

    // private void test4(Stage stage) {
    //     TextArea area = new TextArea();
    //     RangeFormatterParam param = new RangeFormatterParam();
    //     param.setMaxLen(20);
    //     param.setMinLen(3);
    //     RangeTextFormatter textFormatter = new RangeTextFormatter(param);
    //     area.setTextFormatter(textFormatter);
    //     HBox group = new HBox(area);
    //     stage.setScene(new Scene(group, 500, 500));
    //     stage.show();
    // }

    private void test5(Stage stage) {
        TextArea area = new TextArea();
        area.appendText("Tasks: 283 total,   2 running, 281 sleeping,   0 stopped,   0 zombie\n" +
                "%Cpu(s):  0.1 us,  0.1 sy,  0.0 ni, 99.8 id,  0.0 wa,  0.0 hi,  0.1 si,  0.0 st\n" +
                "MiB Mem :   7741.8 total,   5745.6 free,   1191.1 used,    805.1 buff/cache\n" +
                "MiB Swap:   8078.0 total,   8078.0 free,      0.0 used.   6278.8 avail Mem \n" +
                "\n" +
                "    PID USER      PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND \n" +
                "   1138 root      20   0  229268  41048  39212 S   0.3   0.5   0:04.44 sssd_n+ \n" +
                "  83057 root      20   0   65560   5040   4160 R   0.3   0.1   0:00.05 top     \n" +
                "      1 root      20   0  175952  14492   9064 S   0.0   0.2   0:06.80 systemd \n" +
                "      2 root      20   0       0      0      0 S   0.0   0.0   0:00.07 kthrea+ \n" +
                "      3 root       0 -20       0      0      0 I   0.0   0.0   0:00.00 rcu_gp  \n" +
                "      4 root       0 -20       0      0      0 I   0.0   0.0   0:00.00 rcu_pa+ \n" +
                "      6 root       0 -20       0      0      0 I   0.0   0.0   0:00.00 kworke+ ");
        HBox group = new HBox(area);
        stage.setScene(new Scene(group, 500, 500));
        stage.show();
    }

    private void test6(Stage stage) {
        SVGPath svgPath = new SVGPath();


        svgPath.setContent("M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474c-6.1-7.7-15.3-12.2-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1 0.4-12.8-6.3-12.8z");

        svgPath.setScaleX(0.1);
        svgPath.setScaleY(0.1);
        svgPath.setPickOnBounds(true);
        svgPath.setStrokeWidth(0.1);


        //
        // SnapshotParameters parameters = new SnapshotParameters();
        //
        // WritableImage image = new WritableImage(300, 300);
        //
        // svgPath.snapshot(parameters, image);
        //
        // ImageView imageView = new ImageView(image);
        //
        // // ImageView imageView = new ImageView("/easyfx/font/close.svg");
        // imageView.setFitWidth(300);
        // imageView.setFitWidth(300);
        HBox group = new HBox(svgPath);
        // HBox group = new HBox(imageView, svgPath);
        stage.setScene(new Scene(group, 500, 500));
        stage.show();

        System.out.println(svgPath.getStrokeWidth());
        System.out.println(svgPath.prefHeight(-1));
        System.out.println(svgPath.prefWidth(-1));
    }

//    private void test7(Stage stage) {
//
//        SimpleListProperty<Font> l1 = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
//        l1.add(Font.font("Arial", 16));
//
//        SimpleListProperty<String> l2 = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
//        l2.add("Light");
//        l2.add("Dark");
//
//        SimpleListProperty<String> l3 = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
//        l3.add("Light");
//        l3.add("Dark");
//
//        // define the settings to display
//        Setting fontSetting = Setting.of("Font", l1, new ObjectPropertyBase<Font>() {
//            @Override
//            public Object getBean() {
//                return null;
//            }
//
//            @Override
//            public String getName() {
//                return null;
//            }
//        });
//        Setting themeSetting = Setting.of("Theme", l2, new ObjectPropertyBase<String>() {
//            @Override
//            public Object getBean() {
//                return null;
//            }
//
//            @Override
//            public String getName() {
//                return null;
//            }
//        });
//        Setting notificationSetting = Setting.of("Notifications", l3, new ObjectPropertyBase<String>() {
//            @Override
//            public Object getBean() {
//                return null;
//            }
//
//            @Override
//            public String getName() {
//                return null;
//            }
//        });
//
//// define the categories to group the settings
//        Category generalCategory = Category.of("General", fontSetting, themeSetting);
//        Category advancedCategory = Category.of("Advanced", notificationSetting);
//
//// create the PreferencesFX object
//        PreferencesFx preferencesFx = PreferencesFx.of(AppMain.class, generalCategory, advancedCategory);
//
//// show the preferences dialog
//        preferencesFx.show();
//
//    }

    // define a regular expression for numbers
    Pattern pattern1 = Pattern.compile("[{}]");

    Pattern pattern2 = Pattern.compile("\"(\\w*)\"\\s*:");

    Pattern pattern3 = Pattern.compile(":\\s*(\".*?\"|\\d+|true|false|null|\\[.*?\\]|\\{.*?\\})");

//    Pattern numberPattern = Pattern.compile("[0-9]+");

    // define a method to compute the highlighting based on the regular expression
    private StyleSpans<? extends String> computeHighlighting(String text) {
        try {
            StyleSpansBuilder<String> spansBuilder = new StyleSpansBuilder<>();
//        Matcher matcher = numberPattern.matcher(text);
//        int lastKwEnd = 0;
//        while (matcher.find()) {
//            spansBuilder.add("", matcher.start() - lastKwEnd);
//            spansBuilder.add("-fx-fill: red;", matcher.end() - matcher.start());
//            lastKwEnd = matcher.end();
//        }
            Matcher matcher = pattern3.matcher(text);
            int lastKwEnd = 0;
            while (matcher.find()) {
                spansBuilder.add("", matcher.start() - lastKwEnd);
                spansBuilder.add("-fx-fill: blue;", matcher.end() - matcher.start());
                lastKwEnd = matcher.end();
            }

//            matcher = pattern3.matcher(text);
//            lastKwEnd = 0;
//            while (matcher.find()) {
////                spansBuilder.add("", matcher.start() - lastKwEnd);
//                spansBuilder.add("-fx-fill: red;", matcher.end() - matcher.start());
////                lastKwEnd = matcher.end();
//            }

//            matcher = pattern2.matcher(text);
////        lastKwEnd = 0;
//            while (matcher.find()) {
//                spansBuilder.add("", matcher.start() - lastKwEnd);
//                spansBuilder.add("-fx-fill: green;", matcher.end() - matcher.start());
//                lastKwEnd = matcher.end();
//            }
            spansBuilder.add("", text.length() - lastKwEnd);
            return spansBuilder.create();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    private void handlerStyle(InlineCssTextArea area) {
        try {
            Matcher matcher1 = pattern1.matcher(area.getText());
            while (matcher1.find()) {
                area.setStyle(matcher1.start(), matcher1.end(), "-fx-fill: blue;");
            }
            Matcher matcher2 = pattern2.matcher(area.getText());
            while (matcher2.find()) {
                area.setStyle(matcher2.start() + 1, matcher2.end() - 2, "-fx-fill: #EE2C2C;");
//                area.setStyle(matcher2.start() + 1, matcher2.end() - 2, "-fx-fill: red;");
            }
            Matcher matcher3 = pattern3.matcher(area.getText());
            while (matcher3.find()) {
                area.setStyle(matcher3.start() + 3, matcher3.end() - 1, "-fx-fill: green;");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void test8(Stage stage) {


        TextArea textArea = new TextArea();

        textArea.setPrefWidth(500);
        textArea.setPrefHeight(500);

        // create a StyleClassedTextArea
        InlineCssTextArea area = new InlineCssTextArea();

// add a CSS file to the area
        area.getStylesheets().add("test.css");


        String xx = """
                {
                  "name": "Alice",
                  "age": "20",
                  "hobbies": ["reading", "writing", "coding"],
                  "sub" : {
                        "name": "Alice",
                        "age": "20",
                        "hobbies": ["reading", "writing", "coding"]
                  }
                }
                """;


// add some text
        area.appendText(xx);

        textArea.appendText(xx);
//        area.appendText("This is a text with some numbers: 123, 456, 789. {\"a\":\"a\",\"bb\":2}");
//
//// set the style of the first word to red
//        area.setStyle(0, 5, "-fx-fill: red;");
//
//// set the style of the second word to blue
//        area.setStyle(6, 11, "-fx-fill: blue;");
//
//// set the style of the third word to green
//        area.setStyle(12, 17, "-fx-fill: green;");


        area.setPrefWidth(500);
        area.setPrefHeight(500);


//        StyleSpans<? extends String> styleSpans = this.computeHighlighting(area.getText());
//
//        if (styleSpans != null) {
//            // apply the highlighting to the area
//            area.setStyleSpans(0, styleSpans);
//        }


        this.handlerStyle(area);

        HBox group = new HBox(area, textArea);
        stage.setScene(new Scene(group, 1000, 500));
        stage.show();

    }

    private void test9(Stage stage) {
        NumberTextField field = new NumberTextField();
        field.setMaxHeight(25);
        field.setPrefWidth(200);
        field.setMax(10L);
        field.setMin(-5L);
        field.setStep(2L);

        DecimalTextField field1 = new DecimalTextField();
        field1.setMaxHeight(25);
        field1.setPrefWidth(200);
        field1.setMax(21.1d);
        field1.setMin(-5.2);
        field1.setStep(2.1d);

        // NumberTextField1 field1 = new NumberTextField1();
        // field1.setMaxHeight(25);
        // field1.setPrefWidth(200);
        HBox hBox = new HBox(field, field1);
        // // 设置HBox的间距
        hBox.setSpacing(10);
//        stage.setScene(new Scene(field, 200, 25));
        stage.setScene(new Scene(hBox, 300, 300));
        stage.show();

    }

    private void test10(Stage stage) {
        ClearableTextField field = new ClearableTextField();
        field.setPrefHeight(30);
        field.setMaxHeight(30);
        field.setPrefWidth(200);

        // ClearableTextField field1 = new ClearableTextField();
        // field1.setPrefHeight(30);
        // field1.setPrefWidth(200);
        // // 设置文本字段的对齐方式为居中
        // field1.setAlignment(Pos.CENTER);


        // FlexTextField field1 = new FlexTextField();
        // field1.setPrefHeight(100);
        // field1.setClearable(true);
        // field1.setMaxHeight(100);
        // field1.setPrefWidth(200);
        HBox hBox = new HBox(field);
        // HBox hBox = new HBox(field, field1);
        // // 设置HBox的间距
        hBox.setSpacing(10);
        stage.setScene(new Scene(hBox, 300, 300));
        stage.show();

    }

    // private void test11(Stage stage) {
    //     ToggleSwitch toggleSwitch = new ToggleSwitch();
    //     toggleSwitch.setFontSize(15);
    //     toggleSwitch.setSelectedText("开启");
    //     toggleSwitch.setUnselectedText("关闭");
    //
    //     ToggleSwitch toggleSwitch1 = new ToggleSwitch();
    //     toggleSwitch1.setFontSize(12);
    //     toggleSwitch1.setSelectedText("已打开");
    //     toggleSwitch1.setUnselectedText("已关闭");
    //
    //     Button button = new Button("添加");
    //
    //     HBox hBox1 = new HBox(button);
    //     HBox hBox2 = new HBox(toggleSwitch);
    //     HBox hBox3 = new HBox(toggleSwitch1);
    //     VBox vBox = new VBox(hBox1, hBox2, hBox3);
    //     button.setOnAction(actionEvent -> {
    //         ToggleSwitch toggleSwitch2 = new ToggleSwitch();
    //         toggleSwitch2.setFontSize(12);
    //         toggleSwitch2.setSelectedText("已打开");
    //         toggleSwitch2.setUnselectedText("已关闭");
    //
    //         vBox.getChildren().add(new HBox(toggleSwitch2));
    //     });
    //
    //     stage.setScene(new Scene(vBox, 800, 300));
    //     stage.show();
    // }

    private void test12(Stage stage) {
        ClearableTextField textField = new ClearableTextField();
        ClearableTextField textField1 = new ClearableTextField();
        ClearableTextField textField2 = new ClearableTextField();
        ClearableTextField textField3 = new ClearableTextField();

        textField.setRealHeight(20);
        textField1.setRealHeight(25);
        textField2.setRealHeight(30);
        textField3.setRealHeight(50);

        VBox hBox = new VBox(textField, textField1, textField2, textField3);
        // // 设置HBox的间距
        hBox.setSpacing(10);
        stage.setScene(new Scene(hBox, 500, 500));
        stage.show();
    }

    private void test13(Stage stage) {
        NumberTextField textField = new NumberTextField();
        NumberTextField textField1 = new NumberTextField();
        NumberTextField textField2 = new NumberTextField();
        NumberTextField textField3 = new NumberTextField();

        DecimalTextField decimalTextField = new DecimalTextField();
        DecimalTextField decimalTextField1 = new DecimalTextField();
        DecimalTextField decimalTextField2 = new DecimalTextField();
        DecimalTextField decimalTextField3 = new DecimalTextField();

        textField.setRealHeight(20);
        decimalTextField.setRealHeight(20);

        textField1.setRealHeight(25);
        decimalTextField1.setRealHeight(25);

        textField2.setRealHeight(30);
        decimalTextField2.setRealHeight(30);

        textField3.setRealHeight(50);
        decimalTextField3.setRealHeight(50);

        VBox hBox = new VBox(textField, decimalTextField, textField1, decimalTextField1, textField2, decimalTextField2, textField3, decimalTextField3);
        // // 设置HBox的间距
        hBox.setSpacing(10);
        stage.setScene(new Scene(hBox, 500, 500));
        stage.show();
    }

    private void test14(Stage stage) throws InterruptedException {

        Label label1 = new Label("测试内容");
        SearchTextField textField1 = new SearchTextField();
        textField1.setHistoryPopup(new SearchHistoryPopup() {
            @Override
            public List<String> getHistories() {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 50; i++) {
                    list.add("test---------------------" + i);
                }
                return list;
                // return List.of("首个", "1", "2", "最后一个");
            }
        });
        textField1.setFlexWidth("80%");
        textField1.setFlexHeight("20%");
        textField1.setOnSearch(s -> System.out.println("xx:" + s));
        textField1.setRealHeight(35);

        Label label2 = new Label("测试内容");
        SearchTextField textField2 = new SearchTextField();
        textField2.setHistoryPopup(new SearchHistoryPopup() {
            @Override
            public List<String> getHistories() {
                return List.of("3", "4");
            }
        });
        textField2.setOnSearch(s -> System.out.println("xx:" + s));
        textField2.setRealHeight(25);
        textField2.setFlexWidth("80%");
        textField2.setFlexHeight("20%");

        FlexHBox hBox1 = new FlexHBox(label1, textField1);
        FlexHBox hBox2 = new FlexHBox(label2, textField2);
        hBox1.setFlexWidth("100%");
        hBox1.setFlexHeight("50%");
        hBox2.setFlexWidth("100%");
        hBox2.setFlexHeight("50%");
        hBox1.setSpacing(10);
        hBox2.setSpacing(10);
        // // 设置HBox的间距
        FlexVBox vBox = new FlexVBox(hBox1, hBox2);
        vBox.setFlexWidth("100%");
        vBox.setFlexHeight("100%");


        ThreadUtil.start(() -> {
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                int finalI = i;
                FXUtil.runLater(() -> {
                    textField1.setText("com_mysql_jdbc_Driver_" + finalI);
                    textField2.setText("com.mysql.jdbc.Driver." + finalI);
                });
            }
        });

        stage.setScene(new Scene(vBox, 500, 500));
        stage.show();


    }

    private void test15(Stage stage) throws InterruptedException {

        SearchTextField textField1 = new SearchTextField();
        textField1.setHistoryPopup(new SearchHistoryPopup() {
            @Override
            public List<String> getHistories() {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 50; i++) {
                    list.add("test---------------------" + i);
                }
                return list;
                // return List.of("首个", "1", "2", "最后一个");
            }
        });

        // // 设置HBox的间距
        FlexVBox vBox = new FlexVBox(textField1);
        vBox.setFlexWidth("100%");
        vBox.setFlexHeight("100%");

        ExecutorUtil.start(() -> {
            MessageBox.tipMsg("测试消息1", textField1);
        }, 1000);
        ExecutorUtil.start(() -> {
            MessageBox.tipMsg("测试消息2", textField1);
        }, 2000);
        ExecutorUtil.start(() -> {
            MessageBox.tipMsg("测试消息3", textField1);
        }, 3000);
        stage.setScene(new Scene(vBox, 500, 500));
        stage.show();

    }

    private void test16(Stage stage) throws InterruptedException {
        SVGPathExt SVGPathExt = new SVGPathExt("/fx-plus/font/check.svg");
        // fxsvgPath.setScaleX(0.1);
        // fxsvgPath.setScaleY(0.1);
        SVGPathExt.setTranslateX(0.1);
        SVGPathExt.setTranslateY(0.1);

        // fxsvgPath.getTransforms().add(new Scale(2, 2));
        ExecutorUtil.start(() -> {
            System.out.println(SVGPathExt.maxWidth(-1));
            System.out.println(SVGPathExt.maxHeight(-1));
        }, 2000);

        SVGGlyph glyph = new SVGGlyph("/fx-plus/font/check.svg");
        // // 设置HBox的间距
        FlexVBox vBox = new FlexVBox(glyph);
        // FlexVBox vBox = new FlexVBox(fxsvgPath);
        vBox.setFlexWidth("100%");
        vBox.setFlexHeight("100%");


        stage.setScene(new Scene(vBox, 500, 500));
        stage.show();

    }

    private void test17(Stage stage) throws InterruptedException {
        MessageBox.okToast("测试1");
        MessageBox.warnToast("测试2");
        MessageBox.questionToast("测试3");
        // ExecutorUtil.start(() -> {
        //     MessageBox.warnToast("测试2");
        // }, 2000);
        // ExecutorUtil.start(() -> {
        //     MessageBox.questionToast("测试3");
        // }, 4000);

    }

    private void test18(Stage stage) throws InterruptedException {

        VBox vBox = new VBox();

        StateManager stateManager = new StateManager();
        stateManager.setManagedBindVisible(true);

        FXButton btn1 = new FXButton("禁用");
        FXButton btn2 = new FXButton("启用");
        FXButton btn3 = new FXButton("隐藏");
        FXButton btn4 = new FXButton("显示");

        btn1.setOnAction(actionEvent -> stateManager.disable());
        btn2.setOnAction(actionEvent -> stateManager.enable());
        btn3.setOnAction(actionEvent -> stateManager.setVisible(false));
        btn4.setOnAction(actionEvent -> stateManager.setVisible(true));

        FXHBox box1 = new FXHBox();
        box1.addChild(btn1);
        box1.addChild(btn2);
        box1.addChild(btn3);
        box1.addChild(btn4);

        FXButton button1 = new FXButton("测试1");
        FXButton button2 = new FXButton("测试2");
        button1.setStateManager(stateManager);
        button2.setStateManager(stateManager);

        FXHBox box2 = new FXHBox();
        box2.addChild(button1);
        box2.addChild(button2);

        vBox.getChildren().add(box1);
        vBox.getChildren().add(box2);

        stage.setScene(new Scene(vBox, 500, 500));
        stage.show();
    }

    private void test19(Stage stage) throws InterruptedException {

        TitledPane titledPane = new TitledPane();
        titledPane.setContent(new Button("test1"));
        titledPane.setUnderline(false);
        titledPane.setText("数据");
        titledPane.setCollapsible(false);

        TitledPane titledPane1 = new TitledPane();
        titledPane1.setContent(new Button("test2"));
        titledPane1.setUnderline(false);
        titledPane1.setText("内容");
        titledPane1.setCollapsible(false);

        VBox vBox = new VBox(titledPane, titledPane1);

        stage.setScene(new Scene(vBox, 500, 500));
        stage.show();
    }

    private void test20(Stage stage) {
        DecimalTextField decimalTextField = new DecimalTextField();
        decimalTextField.setRealHeight(30);
        decimalTextField.setMin(1d);
        decimalTextField.setMax(10000d);
        decimalTextField.setValue(20d);
        decimalTextField.setScaleLen(5);
        // decimalTextField.setValue(20E-3);

        NumberTextField numberTextField = new NumberTextField();
        numberTextField.setRealHeight(30);
        numberTextField.setMin(1L);
        numberTextField.setMax(20000L);
        numberTextField.setValue(20L);

        VBox hBox = new VBox(decimalTextField, numberTextField);
        // // 设置HBox的间距
        hBox.setSpacing(10);
        stage.setScene(new Scene(hBox, 500, 500));
        stage.show();
    }

    private void test21(Stage stage) {
        ClearableTextField field = new ClearableTextField();
        HBox hBox = new HBox(field);
        RichTreeView treeView = new RichTreeView();
        field.addTextChangeListener((observableValue, string, t1) -> {
            treeView.filter();
        });
        treeView.itemFilter(richTreeItem -> {
            if (field.getText().isEmpty()) {
                return true;
            }
            return richTreeItem.getValue().name().contains(field.getText());
        });

        RichTreeItem<RichTreeItemValue> rootItem = new RichTreeItem<>(treeView);

        RichTreeItemValue rootValue = new RichTreeItemValue();

        rootValue.name("根节点");
        rootItem.setValue(rootValue);

        for (int i = 0; i < 10; i++) {
            RichTreeItem<RichTreeItemValue> level1SubItem = new RichTreeItem<>(treeView);
            RichTreeItemValue level1SubValue = new RichTreeItemValue();
            level1SubValue.name("一级子节点" + i);
            level1SubItem.setValue(level1SubValue);
            rootItem.addChild(level1SubItem);

            for (int j = 0; j < 10; j++) {
                RichTreeItem<RichTreeItemValue> level2SubItem = new RichTreeItem<>(treeView);
                RichTreeItemValue level2SubValue = new RichTreeItemValue();
                level2SubValue.name("二级子节点" + j);
                level2SubItem.setValue(level2SubValue);
                level1SubItem.addChild(level2SubItem);

                for (int k = 0; k < 10; k++) {
                    RichTreeItem<RichTreeItemValue> level3SubItem = new RichTreeItem<>(treeView);
                    RichTreeItemValue level3SubValue = new RichTreeItemValue();
                    level3SubValue.name("三级子节点" + k);
                    level3SubItem.setValue(level3SubValue);
                    level2SubItem.addChild(level3SubItem);
                }
            }
        }
        treeView.root(rootItem);


        VBox vBox = new VBox(hBox, treeView);
        stage.setScene(new Scene(vBox, 500, 500));
        stage.show();
    }

    private void test22(Stage stage) {
        LongAdder adder = new LongAdder();
        TextArea textArea = new TextArea();
        ExecutorUtil.start(() -> {
            FXUtil.runLater(() -> {
                adder.increment();
                textArea.appendText("测试内容" + adder.longValue() + "\n");
            });
        }, 100, 20);
        // 当文本内容改变时，将文本框上滚到顶
        textArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                textArea.setScrollTop(Double.MAX_VALUE);
            }
        });
        VBox vBox = new VBox(textArea);
        stage.setScene(new Scene(vBox, 500, 500));
        stage.show();
    }

    private void test23(Stage stage) {
        LongAdder adder = new LongAdder();
        MsgTextArea textArea = new MsgTextArea();
        textArea.setLineLimit(100L);
        AtomicReference<ScheduledFuture<?>> scheduledFuture = new AtomicReference<>();
        Button start = new Button("开始");
        start.setOnAction(event -> {
            ScheduledFuture<?> future = ExecutorUtil.start(() -> {
                FXUtil.runLater(() -> {
                    adder.increment();
                    textArea.appendText("测试内容" + adder.longValue() + "\n");
                });
            }, 100, 20);
            scheduledFuture.set(future);
        });
        Button pause = new Button("暂停");
        pause.setOnAction(event -> {
            ScheduledFuture<?> future = scheduledFuture.get();
            if (future != null) {
                future.cancel(true);
            }
        });
        HBox hBox = new HBox(start, pause);
        VBox vBox = new VBox(textArea, hBox);

        stage.setScene(new Scene(vBox, 500, 500));
        stage.show();
    }

    private void test24(Stage stage) {
        StageWrapper wrapper = StageManager.newStage(null);
        Message message = new Message("测试", "内容");
        message.setPrefWidth(100);
        message.setPrefHeight(100);
        wrapper.stage().setScene(new Scene(message, 500, 500));
        // wrapper.root().getStylesheets().add(AtlantaFX.CUPERTINO_LIGHT);
        // 设置主题
        wrapper.changeTheme(ThemeManager.currentTheme());
        wrapper.display();

        Notification notification = new Notification("xxx");
        // stage.setScene(new Scene(notification, 500, 500));
        // stage.show();

        // Timeline timeline= Animations.fadeIn(message, new Duration(2000));
        // Timeline timeline = Animations.fadeOut(message, new Duration(2000));
        // Timeline timeline = Animations.flash(message);
        // Timeline timeline = Animations.pulse(message);
        // Timeline timeline = Animations.shakeX(message);
        // Timeline timeline = Animations.shakeY(message);
        Timeline timeline = Animations.wobble(message);
        timeline.play();

        // Popover popover = new Popover(message);
        // // Popover popover = new Popover(notification);
        // popover.setSkin(new PopoverSkin(popover));
        // popover.show(stage);
        // popover.setAutoHide(false);
        //
        // popover.setWidth(100);
        // popover.setHeight(100);
        // stage.show();

    }

    private void test25(Stage stage) {
        Spinner<Double> spinner = new Spinner<>(new DoubleTextValueFactory() {
        }

        );


//        Spinner<Double> spinner = new Spinner<>(new SpinnerValueFactory<Double>() {
//            @Override
//            public void decrement(int i) {
//
//            }
//
//            @Override
//            public void increment(int i) {
//
//            }
//        }
//
//        );

//        DoubleSpinnerValueFactory valueFactory=new DoubleSpinnerValueFactory();

        TextField field = spinner.getEditor();
//        field.setTextFormatter(new TextFormatter<>(new SimpleStringConverter<>() {
//            @Override
//            public Object fromString(String s) {
//                System.out.println(s);
//                return super.fromString(s);
//            }
//        }));

//

//        String textLast;
//        boolean ignore=false;
        field.textProperty().addListener((observableValue, s, t1) -> {


            System.out.println("++++++++");
        });


        field.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // System.out.println("---------");
            // System.out.println(field.getSelection().getStart());
            // System.out.println(field.getSelection().getEnd());
            // System.out.println(field.getCaretPosition());
            if (!TextUtil.checkNumber(event, field)) {
                System.out.println(",,,,,,,,,,,1");
                event.consume();
            }
        });

        field.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            // System.out.println("---------");
            // System.out.println(field.getSelection().getStart());
            // System.out.println(field.getSelection().getEnd());
            // System.out.println(field.getCaretPosition());
            if (!TextUtil.checkNumber(event, field)) {
                System.out.println(",,,,,,,,,,,2");
                event.consume();
            }
        });
        field.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            // System.out.println("---------");
            // System.out.println(field.getSelection().getStart());
            // System.out.println(field.getSelection().getEnd());
            // System.out.println(field.getCaretPosition());
            if (!TextUtil.checkNumber(event, field)) {
                System.out.println(",,,,,,,,,,,2");
                event.consume();
            }
        });

        field.paste();


        Spinner<Integer> spinner2 = new Spinner<>(new IntegerSpinnerValueFactory(0, 100));
        VBox vBox = new VBox(spinner, spinner2);
        spinner.setEditable(true);
        stage.setScene(new Scene(vBox, 500, 500));
        stage.show();
    }

    private void test26(Stage stage) {
        SelectTextFiled filed = new SelectTextFiled();
        filed.setDataList(List.of("a1", "b1", "c1"));
        stage.setScene(new Scene(filed));
        stage.show();
    }

}
