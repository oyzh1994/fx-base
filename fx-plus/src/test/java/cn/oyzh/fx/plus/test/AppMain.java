package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.controls.ToggleSwitch;
import cn.oyzh.fx.plus.ext.ClearableTextField;
import cn.oyzh.fx.plus.ext.DecimalTextField;
import cn.oyzh.fx.plus.ext.NumberTextField;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

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
        test13(stage);
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
        field1.setStep(2.1);

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

    private void test11(Stage stage) {
        ToggleSwitch toggleSwitch = new ToggleSwitch();
        toggleSwitch.setPrefHeight(20);
        toggleSwitch.setMaxHeight(20);
        toggleSwitch.setPrefWidth(100);
        toggleSwitch.setSelectedText("开启");
        toggleSwitch.setUnselectedText("关闭");

        HBox hBox = new HBox(toggleSwitch);
        // // 设置HBox的间距
        hBox.setSpacing(10);
        stage.setScene(new Scene(hBox, 300, 300));
        stage.show();
    }

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
}
