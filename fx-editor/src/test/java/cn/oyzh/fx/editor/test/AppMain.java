package cn.oyzh.fx.editor.test;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.editor.EditorFormatType;
import cn.oyzh.fx.editor.EditorFormatTypeComboBox;
import cn.oyzh.fx.editor.EditorLineNumPolicy;
import cn.oyzh.fx.editor.EditorPane;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.font.FontFamilyComboBox;
import cn.oyzh.fx.plus.font.FontSizeComboBox;
import cn.oyzh.fx.plus.font.FontWeightComboBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Set;


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
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        test1(stage);
        // test2(stage);
        // test3(stage);
    }

    private void test1(Stage stage) {
        FXVBox vBox = new FXVBox();
        vBox.setFlexWidth("100%");
        vBox.setFlexHeight("100%");


        EditorPane editor = new EditorPane();

        editor.setCache(true);
        editor.setCacheHint(CacheHint.SPEED);
        editor.setFlexWidth("100%");
        editor.setFlexHeight("100% - 90");
        editor.setLineNumPolicy(EditorLineNumPolicy.ALWAYS);

        FXHBox hBox = new FXHBox();

        Button btn_1 = new Button("json");
        btn_1.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.json");
            byte[] bytes = IOUtil.readBytes(stream);
            editor.showJsonData(new String(bytes));
        });
        hBox.addChild(btn_1);

        Button btn_2 = new Button("html");
        btn_2.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.html");
            String string = IOUtil.readDefaultString(stream);
            editor.showHtmlData(string);
        });
        hBox.addChild(btn_2);

        Button btn_3 = new Button("xml");
        btn_3.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.xml");
            String string = IOUtil.readDefaultString(stream);
            editor.showXmlData(string);
        });
        hBox.addChild(btn_3);

        Button btn_4 = new Button("yaml");
        btn_4.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.yaml");
            String string = IOUtil.readDefaultString(stream);
            editor.showYamlData(string);
        });
        hBox.addChild(btn_4);

        Button btn_5 = new Button("css");
        btn_5.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.css");
            String string = IOUtil.readDefaultString(stream);
            editor.showCssData(string);
        });
        hBox.addChild(btn_5);

        Button btn_6 = new Button("properties");
        btn_6.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.properties");
            String string = IOUtil.readUtf8String(stream);
            editor.showPropertiesData(string);
        });
        hBox.addChild(btn_6);
        Button btn_7 = new Button("java");
        btn_7.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.java");
            String string = IOUtil.readUtf8String(stream);
            editor.showData(string, EditorFormatType.JAVA);
        });
        hBox.addChild(btn_7);

        FXHBox hBox2 = new FXHBox();

        Button btn_21 = new Button("明亮主题");
        btn_21.setOnAction(actionEvent -> {
            ThemeManager.apply(Themes.PRIMER_LIGHT);
        });
        Button btn_22 = new Button("黑暗主题");
        btn_22.setOnAction(actionEvent -> {
            ThemeManager.apply(Themes.DRACULA);
        });
        hBox2.addChild(btn_21);
        hBox2.addChild(btn_22);
        // 字体大小
        FontSizeComboBox fontSizeComboBox = new FontSizeComboBox();
        fontSizeComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
            editor.setFontSize(t1);
        });
        hBox2.addChild(new FXLabel("字体大小"));
        hBox2.addChild(fontSizeComboBox);
        // 字体名称
        FontFamilyComboBox fontFamilyComboBox = new FontFamilyComboBox();
        fontFamilyComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
            editor.setFontFamily(t1);
        });
        hBox2.addChild(new FXLabel("字体名称"));
        hBox2.addChild(fontFamilyComboBox);
        // 字体字重
        FontWeightComboBox fontWeightComboBox = new FontWeightComboBox();
        fontWeightComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
            editor.setFontWeight(t1);
        });
        hBox2.addChild(new FXLabel("字体字重"));
        hBox2.addChild(fontWeightComboBox);

        FXHBox hBox3 = new FXHBox();
        // 高亮
        FXTextField text_31 = new FXTextField();
        text_31.setPromptText("查找内容");
        editor.highlightTextProperty().bind(text_31.textProperty());
        hBox3.addChild(text_31);

        // 提示词
        FXTextField text_32 = new FXTextField();
        text_32.setPromptText("提示词");
        text_32.addTextChangeListener((observableValue, s, t1) -> {
            if (StringUtil.isEmpty(t1)) {
                return;
            }
            if (t1.contains(",")) {
                editor.setPrompts(Set.of(t1.split(",")));
            } else {
                editor.setPrompts(Set.of(t1));
            }
        });
        hBox3.addChild(text_32);
        // 格式
        EditorFormatTypeComboBox formatTypeComboBox = new EditorFormatTypeComboBox();
        formatTypeComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
            editor.setFormatType(t1);
        });
        editor.formatTypeProperty().addListener((observableValue, formatType, t1) -> {
            formatTypeComboBox.setValue(t1);
        });
        hBox3.addChild(new FXLabel("内容格式"));
        hBox3.addChild(formatTypeComboBox);

        vBox.addChild(hBox);
        vBox.addChild(hBox2);
        vBox.addChild(hBox3);
        vBox.addChild(editor);

        Scene scene = new Scene(vBox);

        stage.setWidth(800);
        stage.setHeight(600);

        stage.setScene(scene);
        stage.show();
    }

    // private void test2(Stage stage) {
    //     FXVBox vBox = new FXVBox();
    //     vBox.setFlexWidth("100%");
    //     vBox.setFlexHeight("100%");
    //
    //
    //     // EditorPane editor = new EditorPane();
    //     InlineCssTextArea editor = new InlineCssTextArea();
    //     editor.setParagraphGraphicFactory(new RichLineNumberFactory(editor));
    //     editor.prefWidthProperty().bind(vBox.widthProperty());
    //     editor.prefHeightProperty().bind(vBox.heightProperty().subtract(100));
    //     // editor.setFlexWidth("100%");
    //     // editor.setFlexHeight("100% - 60");
    //     // editor.setLineNumPolicy(EditorLineNumPolicy.ALWAYS);
    //
    //
    //     // editor.getContent().setAutoScrollOnDragDesired(false);
    //
    //     AtomicBoolean ignore = new AtomicBoolean(false);
    //     // editor.getContent().estimatedScrollYProperty().addListener((observableValue, aDouble, t1) -> {
    //     //     // if(editor.isIgnoreVChanged()){
    //     //     //     return;
    //     //     // }
    //     //     //  System.out.println(t1);
    //     //     //  // editor.initTextStyle();
    //     //     //  if (ignore.get()) {
    //     //     //      ignore.set(false);
    //     //     //  } else {
    //     //     //      ignore.set(true);
    //     //     //      editor.initTextStyle();
    //     //     //  }
    //     //     System.out.println(t1);
    //     // });
    //
    //     // editor.addEventFilter(ScrollEvent.SCROLL, e -> {
    //     //     System.out.println("----1");
    //     //     editor.initTextStyle();
    //     // });
    //     //
    //     // editor.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
    //     //     if (e.getCode() == KeyCode.UP
    //     //             || e.getCode() == KeyCode.DOWN) {
    //     //         System.out.println("----2");
    //     //         editor.initTextStyle();
    //     //     } else if (e.getCode() == KeyCode.PAGE_UP
    //     //             || e.getCode() == KeyCode.PAGE_DOWN) {
    //     //         System.out.println("----3");
    //     //         editor.initTextStyleDelay();
    //     //     }
    //     // });
    //
    //     // editor.getContent().getVisibleParagraphs().addListener((ListChangeListener<Paragraph<String, String, String>>) change -> {
    //     //     if (ignore.get()) {
    //     //         ignore.set(false);
    //     //     } else {
    //     //         ignore.set(true);
    //     //         editor.initTextStyle();
    //     //     }
    //     // });
    //
    //
    //     // editor.getContent().getVisibleParagraphs().addListener((ListChangeListener<Paragraph<String, String, String>>) change -> {
    //     //
    //     //     System.out.println("--------------1");
    //     //     // for (Paragraph<String, String, String> visibleParagraph : editor.getContent().getVisibleParagraphs()) {
    //     //     //
    //     //     //     System.out.println(visibleParagraph.getText() );
    //     //     //
    //     //     //     visibleParagraph.getStyleSpans().iterator().next().
    //     //     // }
    //     //     try {
    //     //         TwoDimensional.Position index1 = editor.getContent().offsetToPosition(0, TwoDimensional.Bias.Backward);
    //     //         TwoDimensional.Position index2 = editor.getContent().offsetToPosition(100, TwoDimensional.Bias.Backward);
    //     //         if (index1 != null && index2 != null) {
    //     //             System.out.println(index1.getMajor() + "=" + index2.getMajor());
    //     //         }
    //     //     }catch (Exception ex){
    //     //
    //     //     }
    //     //     System.out.println("--------------2");
    //     // });
    //
    //
    //     FXHBox hBox = new FXHBox();
    //
    //     FXTextField textField = new FXTextField();
    //     // textField.addTextChangeListener((observableValue, s, t1) -> {
    //     //     editor.setHighlightText(t1);
    //     // });
    //     // editor.highlightTextProperty().bind(textField.textProperty());
    //     hBox.addChild(textField);
    //
    //     EditorFormatTypeComboBox formatTypeComboBox = new EditorFormatTypeComboBox();
    //     // editor.formatTypeProperty().bind(formatTypeComboBox.selectedItemProperty());
    //
    //     // formatTypeComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
    //     //     editor.setFormatType(t1);
    //     // });
    //     hBox.addChild(formatTypeComboBox);
    //
    //     Button btn_1 = new Button("json数据");
    //     btn_1.setOnAction(actionEvent -> {
    //         InputStream stream = ResourceUtil.getResourceAsStream("test.json");
    //         byte[] bytes = IOUtil.readBytes(stream);
    //         editor.replaceText(new String(bytes));
    //     });
    //     hBox.addChild(btn_1);
    //
    //     Button btn_2 = new Button("html数据");
    //     btn_2.setOnAction(actionEvent -> {
    //         InputStream stream = ResourceUtil.getResourceAsStream("test.html");
    //         String string = IOUtil.readDefaultString(stream);
    //         editor.replaceText(string);
    //     });
    //     hBox.addChild(btn_2);
    //
    //     Button btn_3 = new Button("xml数据");
    //     btn_3.setOnAction(actionEvent -> {
    //         InputStream stream = ResourceUtil.getResourceAsStream("test.xml");
    //         String string = IOUtil.readDefaultString(stream);
    //         editor.replaceText(string);
    //     });
    //     hBox.addChild(btn_3);
    //
    //     Button btn_4 = new Button("yaml数据");
    //     btn_4.setOnAction(actionEvent -> {
    //         InputStream stream = ResourceUtil.getResourceAsStream("test.yaml");
    //         String string = IOUtil.readDefaultString(stream);
    //         editor.replaceText(string);
    //     });
    //     hBox.addChild(btn_4);
    //
    //     Button btn_5 = new Button("css数据");
    //     btn_5.setOnAction(actionEvent -> {
    //         InputStream stream = ResourceUtil.getResourceAsStream("test.css");
    //         String string = IOUtil.readDefaultString(stream);
    //         editor.replaceText(string);
    //     });
    //     hBox.addChild(btn_5);
    //
    //     Button btn_6 = new Button("properties数据");
    //     btn_6.setOnAction(actionEvent -> {
    //         InputStream stream = ResourceUtil.getResourceAsStream("test.properties");
    //         String string = IOUtil.readUtf8String(stream);
    //         editor.replaceText(string);
    //     });
    //     hBox.addChild(btn_6);
    //
    //     FXHBox hBox2 = new FXHBox();
    //
    //     Button btn_21 = new Button("明亮主题");
    //     btn_21.setOnAction(actionEvent -> {
    //         // Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
    //         ThemeManager.apply(Themes.PRIMER_LIGHT);
    //     });
    //     Button btn_22 = new Button("黑暗主题");
    //     btn_22.setOnAction(actionEvent -> {
    //         // Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
    //         ThemeManager.apply(Themes.DRACULA);
    //     });
    //     hBox2.addChild(btn_21);
    //     hBox2.addChild(btn_22);
    //
    //     vBox.addChild(hBox);
    //     vBox.addChild(hBox2);
    //     vBox.addChild(editor);
    //
    //     Scene scene = new Scene(vBox);
    //
    //     stage.setWidth(800);
    //     stage.setHeight(600);
    //
    //     stage.setScene(scene);
    //     stage.show();
    // }
    //
    // public void test3(Stage stage) {
    //
    //     FXHBox hBox = new FXHBox();
    //
    //     BaseRichTextArea editor = new BaseRichTextArea();
    //
    //
    //     Button btn_1 = new Button("json数据");
    //     btn_1.setOnAction(actionEvent -> {
    //         InputStream stream = ResourceUtil.getResourceAsStream("test.json");
    //         byte[] bytes = IOUtil.readBytes(stream);
    //         String text = new String(bytes);
    //         editor.setText(text);
    //         Matcher matcher = RegexHelper.jsonPattern().matcher(text);
    //         // 状态标记：明确区分当前解析位置
    //         boolean inObject = false;    // 是否在对象内（{}中）
    //         boolean expectKey = false;   // 是否期待键（对象内，逗号/左括号后）
    //         boolean expectValue = false; // 是否期待值（冒号后）
    //         while (matcher.find()) {
    //             // 处理对象开始
    //             if (matcher.group("braceOpen") != null) {
    //                 inObject = true;
    //                 expectKey = true; // 对象内首先期待键
    //             }
    //             // 处理对象结束
    //             else if (matcher.group("braceClose") != null) {
    //                 inObject = false;
    //                 expectKey = false;
    //                 expectValue = false;
    //             }
    //             // 处理数组开始
    //             else if (matcher.group("bracketOpen") != null) {
    //                 expectValue = true; // 数组内直接期待值
    //             }
    //             // 处理数组结束
    //             else if (matcher.group("bracketClose") != null) {
    //                 expectValue = inObject; // 数组结束后是否期待值取决于是否在对象内
    //             }
    //             // 处理逗号分隔符
    //             else if (matcher.group("comma") != null) {
    //                 if (inObject) {
    //                     expectKey = true;  // 对象内逗号后期待新键
    //                 } else {
    //                     expectValue = true; // 数组内逗号后期待新值
    //                 }
    //             }
    //             // 处理冒号（键值分隔）
    //             else if (matcher.group("colon") != null) {
    //                 expectKey = false;
    //                 expectValue = true; // 冒号后必然期待值
    //             }
    //             // 处理字符串（根据状态判断是键还是值）
    //             else if (matcher.group("string") != null) {
    //                 if (expectKey) {
    //                     expectKey = false;
    //                     // RichTextStyle style = new RichTextStyle(matcher.start("string") + fIndex, matcher.end("string") + fIndex, KEY_STYLE);
    //                     editor.setStyle(matcher.start("string"), matcher.end("string"), Color.BLUE);
    //                     // styles.add(style);
    //                 } else if (expectValue) {
    //                     expectValue = false;
    //                     // RichTextStyle style = new RichTextStyle(matcher.start("string") + fIndex, matcher.end("string") + fIndex, VALUE_STYLE);
    //                     editor.setStyle(matcher.start("string"), matcher.end("string"), Color.GREEN);
    //                     // this.setStyle(style);
    //                     // styles.add(style);
    //                 }
    //             } else if (matcher.group("keyword") != null && expectValue) {  // 处理关键字值
    //                 // RichTextStyle style = new RichTextStyle(matcher.start("keyword") + fIndex, matcher.end("keyword") + fIndex, KEYWORD_STYLE);
    //                 // editor.setStyle(matcher.start("keyword"), matcher.end("keyword"), Color.RED);
    //                 // this.setStyle(style);
    //                 // styles.add(style);
    //                 expectValue = false;
    //             } else if (matcher.group("number") != null && expectValue) { // 处理数字值
    //                 // RichTextStyle style = new RichTextStyle(matcher.start("number") + fIndex, matcher.end("number") + fIndex, KEYWORD_STYLE);
    //                 // this.setStyle(style);
    //                 // styles.add(style);
    //                 expectValue = false;
    //             }
    //         }
    //     });
    //     // hBox.addChild(btn_1);
    //
    //     // editor.setStyle(1, 3, Color.BLUE);
    //     // editor.setStyle(4, 14, Color.RED);
    //
    //     editor.setLineNumbersEnabled(true);
    //
    //     stage.setScene(new Scene(new VBox(hBox, editor)));
    //     Button btn1 = new Button("重做");
    //     Button btn2 = new Button("撤销");
    //     Button btn3 = new Button("清除");
    //     hBox.addChild(btn_1);
    //     hBox.addChild(btn1);
    //     hBox.addChild(btn2);
    //     hBox.addChild(btn3);
    //
    //     btn1.setOnAction(e -> {
    //         editor.redo();
    //     });
    //     btn2.setOnAction(e -> {
    //         editor.undo();
    //     });
    //     btn3.setOnAction(e -> {
    //         editor.setStyle(0, 20, null);
    //     });
    //     stage.setWidth(800);
    //     stage.setHeight(600);
    //     stage.show();
    //
    // }

}
