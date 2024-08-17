package cn.oyzh.fx.rich.test;

import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.rich.RichTextStyle;
import cn.oyzh.fx.rich.richtextarea.control.FlexRichTextArea;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;


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
        test1(stage);
        // test2(stage);
        // test3(stage);
    }

    private void test1(Stage stage) {
        FlexRichTextArea editor = new FlexRichTextArea();
        editor.setAutoSave(true);
        editor.setFlexWidth("100%");
        editor.setFlexHeight("100% - 100");

        FlexHBox hbox = new FlexHBox();
        hbox.setFlexWidth("100%");
        hbox.setRealHeight(50);

        Button btn1 = new Button("设置文本");
        btn1.setOnAction(event -> editor.setText("Hello rich textarea"));
        hbox.addChild(btn1);

        Button btn2 = new Button("获取文本");
        btn2.setOnAction(event -> MessageBox.info(editor.getText()));
        hbox.addChild(btn2);

        Button btn3 = new Button("追加文本");
        btn3.setOnAction(event -> editor.append("追加内容"));
        hbox.addChild(btn3);

        Button btn4 = new Button("追加一行");
        btn4.setOnAction(event -> editor.appendLine("新文本行"));
        hbox.addChild(btn4);

        Button btn5 = new Button("清空内容");
        btn5.setOnAction(event -> editor.clear());
        hbox.addChild(btn5);

        RichTextStyle style1 = new RichTextStyle(0, 5, "red");
        Button btn6 = new Button("设置颜色1");
        btn6.setOnAction(event -> editor.setStyle(style1));
        hbox.addChild(btn6);

        RichTextStyle style2 = new RichTextStyle(4, 10, "blue");
        Button btn7 = new Button("设置颜色2");
        btn7.setOnAction(event -> editor.setStyle(style2));
        hbox.addChild(btn7);

        Button btn8 = new Button("清除颜色");
        btn8.setOnAction(event -> editor.clearTextStyle());
        hbox.addChild(btn8);

        FlexHBox hbox2 = new FlexHBox();
        hbox2.setFlexWidth("100%");
        hbox2.setRealHeight(50);

        Button btn10 = new Button("撤销");
        btn10.setOnAction(event -> editor.undo());
        hbox2.addChild(btn10);

        Button btn11 = new Button("重做");
        btn11.setOnAction(event -> editor.redo());
        hbox2.addChild(btn11);

        Button btn12 = new Button("删除文字");
        btn12.setOnAction(event -> editor.deleteText(0, 10));
        hbox2.addChild(btn12);

        Button btn13 = new Button("移动光标");
        btn13.setOnAction(event -> editor.positionCaret(10));
        hbox2.addChild(btn13);

        Button btn14 = new Button("插入文字");
        btn14.setOnAction(event -> editor.insertText("插入内容"));
        hbox2.addChild(btn14);

        FlexVBox root = new FlexVBox(hbox, hbox2, editor);

        root.setPrefHeight(500);
        root.setPrefWidth(800);

        stage.setScene(new Scene(root));
        stage.show();


    }

    private void test2(Stage stage) {
        RichTextArea editor = new RichTextArea();
        editor.setAutoSave(true);

        FlexHBox hbox = new FlexHBox();
        hbox.setFlexWidth("100%");
        hbox.setRealHeight(50);


        Button btn7 = new Button("设置颜色2");
        btn7.setOnAction(event -> {
            String text = "1234567";
            TextDecoration textDecoration3 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).foreground("red").build();
            ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
            Document document = new Document(text,
                    List.of(new DecorationModel(0, 7, textDecoration3, paragraphDecoration)), text.length());
            editor.getActionFactory().open(document).execute(new ActionEvent());
            editor.setAutoSave(true);
        });
        hbox.addChild(btn7);

        FlexVBox root = new FlexVBox(hbox, editor);
        root.setPrefHeight(500);
        root.setPrefWidth(800);

        stage.setScene(new Scene(root));
        stage.show();


    }

    private void test3(Stage stage) {
        RichTextArea editor = new RichTextArea();
        editor.setAutoSave(true);

        HBox hbox = new HBox();


        Button btn7 = new Button("设置颜色2");
        btn7.setOnAction(event -> {
            String text = "1234567";
            TextDecoration textDecoration3 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).foreground("red").build();
            ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
            Document document = new Document(text,
                    List.of(new DecorationModel(0, 7, textDecoration3, paragraphDecoration)), text.length());
            editor.getActionFactory().open(document).execute(new ActionEvent());
            editor.setAutoSave(true);
        });
        hbox.getChildren().add(btn7);

        VBox root = new VBox(hbox, editor);
        root.setPrefHeight(500);
        root.setPrefWidth(800);

        stage.setScene(new Scene(root));
        stage.show();


    }


}
