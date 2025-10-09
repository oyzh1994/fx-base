package cn.oyzh.fx.rich.test;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.rich.richtextfx.control.FXVirtualizedScrollPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 * @author oyzh
 * @since 2025-09-24
 */
public class TextMate4RichTextFXTest extends Application {

    public static void main(String[] args) {
        launch(TextMate4RichTextFXTest.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // ThemeManager.apply(Themes.PRIMER_LIGHT);
        // Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
        test1(stage);
    }

    private void test1(Stage stage) {
        CodeArea editor = new CodeArea();

        TextMateSyntaxHighlighter highlighter = new TextMateSyntaxHighlighter();
        highlighter.setCodeArea(editor);
        String languageGrammarFile = "/grammars/c.tmLanguage.json";
        // highlighter.setupSyntaxHighlighting(languageGrammarFile, null);

        String exampleFile = "/examples/c.example.c";
        InputStream stream = ResourceUtil.getResourceAsStream(exampleFile);
        String text = FileUtil.readString(stream, Charset.defaultCharset());
        editor.replaceText(text);



        FXVirtualizedScrollPane<CodeArea> scrollPane = new FXVirtualizedScrollPane(editor);
        // scrollPane.maxHeightProperty().bind(editor.maxHeightProperty());
        // scrollPane.maxWidthProperty().bind(editor.maxWidthProperty());
        // editor.setPrefHeight(480);
        scrollPane.setFlexHeight("100%");
        FXVBox vBox = new FXVBox();
        vBox.setFlexHeight("100%");
        vBox.getChildren().add(scrollPane);
        stage.setScene(new Scene(vBox));
        stage.setWidth(600);
        stage.setHeight(480);
        stage.show();

    }

    public static class TextMate4RichTextFXTestStarter {

        public static void main(String[] args) {
            TextMate4RichTextFXTest.main(args);
        }
    }

}
