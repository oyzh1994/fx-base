package cn.oyzh.fx.rich.test;

import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.rich.richtextarea.RichTextColor;
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
        editor.setFlexHeight("100% - 50");

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

        RichTextColor textColor1 = new RichTextColor(0, 5, Color.RED, 1);
        Button btn6 = new Button("设置颜色1");
        btn6.setOnAction(event -> editor.setColor(textColor1));
        hbox.addChild(btn6);

        RichTextColor textColor2 = new RichTextColor(6, 10, Color.BLUE, 1);
        Button btn7 = new Button("设置颜色2");
        btn7.setOnAction(event -> editor.setColor(textColor2));
        hbox.addChild(btn7);

        VBox vbox = new VBox(editor);
        vbox.setPrefHeight(200);
        vbox.setPrefWidth(200);
        VBox vbox1 = new VBox(vbox);
        vbox1.setPrefHeight(200);
        vbox1.setPrefWidth(200);
        FlexVBox root = new FlexVBox(hbox, vbox1){

            // @Override
            // public void changeTheme(ThemeStyle style) {
            // }
        };

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
