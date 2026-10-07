package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.fx.editor.incubator.Editor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;


/**
 * incubator 模块编辑器字体测试，对比自定义字体与默认字体的显示效果。
 *
 * @author oyzh
 * @since 2022/5/18
 */
public class EditorFontTest extends Application {

    public static void main(String[] args) {
        launch(EditorFontTest.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        test1(new Stage());
        test2(new Stage());
        stage.setTitle("编辑器测试");
    }

    public static Font loadCustomFont(String path, double size) {
        Font font = Font.loadFont(EditorFontTest.class.getResourceAsStream(path), size);
        return font;
    }

    private void test1(Stage stage) {

        Editor editor = new Editor();
        editor.setEditorFont(loadCustomFont("/JetBrainsMono-2.304/fonts/ttf/JetBrainsMono-Regular.ttf", 14));

        editor.setText("""
                123456789
                abcdefghijk
                一二三四五六七八九张三
                """);
        editor.setFlexWidth("100%");
        editor.setFlexHeight("100%");
        editor.showLineNum();

        Scene scene = new Scene(editor);

        stage.setWidth(800);
        stage.setHeight(600);

        stage.setScene(scene);
        stage.show();

    }

    private void test2(Stage stage) {

        Editor editor = new Editor();
        //        editor.setEditorFont(loadCustomFont("/JetBrainsMono-2.304/fonts/ttf/JetBrainsMono-Regular.ttf",14));

        editor.setText("""
                123456789
                abcdefghijk
                一二三四五六七八九张三
                """);
        editor.setFlexWidth("100%");
        editor.setFlexHeight("100%");
        editor.showLineNum();

        Scene scene = new Scene(editor);

        stage.setWidth(800);
        stage.setHeight(600);

        stage.setScene(scene);
        stage.show();

    }

    public static class EditorFontTestStarter {

        public static void main(String[] args) {
            EditorFontTest.main(args);
        }

    }

}
