package cn.oyzh.fx.rich.test;

import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;


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
        Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
//        test1(stage);
        // test2(stage);
        // test3(stage);
        // test4(stage);
//        test5(stage);
//        test6(stage);
    }

//    private void test1(Stage stage) {
//        FlexRichTextArea editor = new FlexRichTextArea();
//        editor.setAutoSave(true);
//        editor.setFlexWidth("100%");
//        editor.setFlexHeight("100% - 100");
//
//        FlexHBox hbox = new FlexHBox();
//        hbox.setFlexWidth("100%");
//        hbox.setRealHeight(50);
//
//        Button btn1 = new Button("设置文本");
//        btn1.setOnAction(event -> editor.setText("Hello rich textarea"));
//        hbox.addChild(btn1);
//
//        Button btn2 = new Button("获取文本");
//        btn2.setOnAction(event -> MessageBox.info(editor.getText()));
//        hbox.addChild(btn2);
//
//        Button btn3 = new Button("追加文本");
//        btn3.setOnAction(event -> editor.append("追加内容"));
//        hbox.addChild(btn3);
//
//        Button btn4 = new Button("追加一行");
//        btn4.setOnAction(event -> editor.appendLine("新文本行"));
//        hbox.addChild(btn4);
//
//        Button btn5 = new Button("清空内容");
//        btn5.setOnAction(event -> editor.clear());
//        hbox.addChild(btn5);
//
//        RichTextStyle style1 = new RichTextStyle(0, 5, "red");
//        Button btn6 = new Button("设置颜色1");
//        btn6.setOnAction(event -> editor.setStyle(style1));
//        hbox.addChild(btn6);
//
//        RichTextStyle style2 = new RichTextStyle(4, 10, "blue");
//        Button btn7 = new Button("设置颜色2");
//        btn7.setOnAction(event -> editor.setStyle(style2));
//        hbox.addChild(btn7);
//
//        Button btn8 = new Button("清除颜色");
//        btn8.setOnAction(event -> editor.clearTextStyle());
//        hbox.addChild(btn8);
//
//        FlexHBox hbox2 = new FlexHBox();
//        hbox2.setFlexWidth("100%");
//        hbox2.setRealHeight(50);
//
//        Button btn10 = new Button("撤销");
//        btn10.setOnAction(event -> editor.undo());
//        hbox2.addChild(btn10);
//
//        Button btn11 = new Button("重做");
//        btn11.setOnAction(event -> editor.redo());
//        hbox2.addChild(btn11);
//
//        Button btn12 = new Button("删除文字");
//        btn12.setOnAction(event -> editor.deleteText(0, 10));
//        hbox2.addChild(btn12);
//
//        Button btn13 = new Button("移动光标");
//        btn13.setOnAction(event -> editor.positionCaret(10));
//        hbox2.addChild(btn13);
//
//        Button btn14 = new Button("插入文字");
//        btn14.setOnAction(event -> editor.insertText("插入内容"));
//        hbox2.addChild(btn14);
//
//        Button btn15 = new Button("替换文字");
//        btn15.setOnAction(event -> editor.replaceSelection("替换内容"));
//        hbox2.addChild(btn15);
//
//        Button btn16 = new Button("清除历史");
//        btn16.setOnAction(event -> editor.forgetHistory());
//        hbox2.addChild(btn16);
//
//        Button btn17 = new Button("选中区间");
//        btn17.setOnAction(event -> editor.selectRange(2, 8));
//        hbox2.addChild(btn17);
//
//        FlexVBox root = new FlexVBox(hbox, hbox2, editor);
//
//        editor.addTextChangeListener((observableValue, s, t1) -> {
//            System.out.println(t1);
//        });
//
//        root.setPrefHeight(500);
//        root.setPrefWidth(800);
//
//        stage.setScene(new Scene(root));
//        stage.show();
//
//
//    }

//    private void test2(Stage stage) {
//        RichTextArea editor = new RichTextArea();
//        editor.setAutoSave(true);
//
//        FXHBox hbox = new FXHBox();
//        hbox.setFlexWidth("100%");
//        hbox.setRealHeight(50);
//
//
//        Button btn7 = new Button("设置颜色2");
//        btn7.setOnAction(event -> {
//            String text = "1234567";
//            TextDecoration textDecoration3 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).foreground("red").build();
//            ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
//            Document document = new Document(text,
//                    List.of(new DecorationModel(0, 7, textDecoration3, paragraphDecoration)), text.length());
//            editor.getActionFactory().open(document).execute(new ActionEvent());
//            editor.setAutoSave(true);
//        });
//        hbox.addChild(btn7);
//
//        FXVBox root = new FXVBox(hbox, editor);
//        root.setPrefHeight(500);
//        root.setPrefWidth(800);
//
//        stage.setScene(new Scene(root));
//        stage.show();
//
//
//    }
//
//    private void test3(Stage stage) {
//        RichTextArea editor = new RichTextArea();
//        editor.setAutoSave(true);
//
//        HBox hbox = new HBox();
//
//
//        Button btn7 = new Button("设置颜色2");
//        btn7.setOnAction(event -> {
//            String text = "1234567";
//            TextDecoration textDecoration3 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).foreground("red").build();
//            ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
//            Document document = new Document(text,
//                    List.of(new DecorationModel(0, 7, textDecoration3, paragraphDecoration)), text.length());
//            editor.getActionFactory().open(document).execute(new ActionEvent());
//            editor.setAutoSave(true);
//        });
//        hbox.getChildren().add(btn7);
//
//        LVBox root = new LVBox(hbox, editor);
//        root.setPrefHeight(500);
//        root.setPrefWidth(800);
//
//        stage.setScene(new Scene(root));
//        stage.show();
//
//
//    }

//    public void test4(Stage stage) {
//
//        // CodeArea richTextArea = new CodeArea();
//        // RichTextArea richTextArea = new RichTextArea();
//        FXRichTextArea richTextArea = new FXRichTextArea();
//        richTextArea.appendText("test1 test2");
//        StyleAttribute<Color> attribute = StyleAttributeMap.TEXT_COLOR;
//
//        StyleAttributeMap attributeMap = StyleAttributeMap.of(attribute, Color.BLUE);
//        TextPos start = TextPos.ofLeading(0, 0);
//        TextPos end = TextPos.ofLeading(0, 5);
//        richTextArea.setStyle(start, end, attributeMap);
//
//        StyleAttribute<Color> attribute1 = StyleAttributeMap.TEXT_COLOR;
//
//        StyleAttributeMap attributeMap1 = StyleAttributeMap.of(attribute1, Color.RED);
//        TextPos start1 = TextPos.ofLeading(0, 6);
//        TextPos end1 = TextPos.ofLeading(0, 11);
//        richTextArea.setStyle(start1, end1, attributeMap1);
//
// //        richTextArea.setLineNumbersEnabled(true);
//
//
//        stage.setScene(new Scene(richTextArea));
//        stage.show();
//    }
//
//    public void test5(Stage stage) {
//
//        FXHBox hBox = new FXHBox();
//
//        FXRichTextArea richTextArea = new FXRichTextArea();
//
//        richTextArea.setWrapText(true);
//        richTextArea.appendText("test1 test2");
//        StyleAttribute<Color> attribute = StyleAttributeMap.TEXT_COLOR;
//
//        StyleAttributeMap attributeMap = StyleAttributeMap.of(attribute, Color.BLUE);
//        TextPos start = TextPos.ofLeading(0, 1);
//        TextPos end = TextPos.ofLeading(0, 3);
//        richTextArea.setStyle(start, end, attributeMap);
//
//        StyleAttribute<Color> attribute1 = StyleAttributeMap.TEXT_COLOR;
//
//        StyleAttributeMap attributeMap1 = StyleAttributeMap.of(attribute1, Color.RED);
//        TextPos start1 = TextPos.ofLeading(0, 4);
//        TextPos end1 = TextPos.ofLeading(0, 9);
//        richTextArea.setStyle(start1, end1, attributeMap1);
//
//        richTextArea.setLineNumbersEnabled(true);
//
//        stage.setScene(new Scene(new LVBox(hBox, richTextArea)));
//        Button btn1 = new Button("重做");
//        Button btn2 = new Button("撤销");
//        hBox.addChild(btn1);
//        hBox.addChild(btn2);
//
//        btn1.setOnAction(e -> {
//            richTextArea.redo();
//        });
//        btn2.setOnAction(e -> {
//            richTextArea.undo();
//        });
//        stage.show();
//
//    }
//
//    public void test6(Stage stage) {
//
//        FXHBox hBox = new FXHBox();
//
//        BaseRichTextArea richTextArea = new BaseRichTextArea();
//        richTextArea.appendText("0123456789\n");
//        richTextArea.appendText("0123456789");
//
//        richTextArea.setStyle(1, 3, Color.BLUE);
//        richTextArea.setStyle(4, 14, Color.RED);
//
//        richTextArea.setLineNumbersEnabled(true);
//
//        stage.setScene(new Scene(new VBox(hBox, richTextArea)));
//        Button btn1 = new Button("重做");
//        Button btn2 = new Button("撤销");
//        Button btn3 = new Button("清除");
//        hBox.addChild(btn1);
//        hBox.addChild(btn2);
//        hBox.addChild(btn3);
//
//        btn1.setOnAction(e -> {
//            richTextArea.redo();
//        });
//        btn2.setOnAction(e -> {
//            richTextArea.undo();
//        });
//        btn3.setOnAction(e -> {
//            richTextArea.setStyle(0, 20, null);
//        });
//        stage.show();
//
//    }

    public void test7(Stage stage) {

        FXHBox hBox = new FXHBox();

        CodeArea codeArea = new CodeArea();
        codeArea.appendText("0123456789\n");
        codeArea.appendText("0123456789");


        stage.show();

    }

}
