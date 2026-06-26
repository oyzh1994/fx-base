package cn.oyzh.fx.editor.test.incubator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import jfx.incubator.scene.control.richtext.CodeArea;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class MemoryLeakTest extends Application {

    public static void main(String[] args) {
        launch(MemoryLeakTest.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        test1(stage);
        //        test2(stage);
        stage.setTitle("memory leak test");
    }

    private void watch(Object o) {
        WeakReference<?> reference = new WeakReference<>(o);
        new Thread(() -> {
            while (true) {
                if (reference.get() != null) {
                    System.out.println("object is alive");
                } else {
                    System.out.println("object is die");
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void test1(Stage stage) {
        Button button = new Button("new page");
        button.setOnAction(event -> {
            Stage stage1 = new Stage();
            CodeArea codeArea = new CodeArea();
            codeArea.appendText("hello code area");
            Scene scene1 = new Scene(codeArea);
            stage1.setScene(scene1);
            stage1.setWidth(800);
            stage1.setHeight(600);
            stage1.show();
            stage1.setOnHidden(event1 -> {
                codeArea.getSkin().dispose();
                System.out.println("close----");
                Executors.newScheduledThreadPool(1).schedule(System::gc, 3, TimeUnit.SECONDS);
            });
            this.watch(stage1);
        });

        Scene scene = new Scene(button);
        stage.setScene(scene);
        stage.setWidth(300);
        stage.setHeight(200);
        stage.show();
    }

    private void test2(Stage stage) {
        Button button = new Button("new page");
        button.setOnAction(event -> {
            Stage stage1 = new Stage();
            TextArea textArea = new TextArea();
            textArea.appendText("hello text area");
            Scene scene1 = new Scene(textArea);
            stage1.setScene(scene1);
            stage1.setWidth(800);
            stage1.setHeight(600);
            stage1.show();
            stage1.setOnHidden(event1 -> {
                textArea.getSkin().dispose();
                System.out.println("close----");
                Executors.newScheduledThreadPool(1).schedule(System::gc, 3, TimeUnit.SECONDS);
            });
            this.watch(stage1);
        });

        Scene scene = new Scene(button);
        stage.setScene(scene);
        stage.setWidth(300);
        stage.setHeight(200);
        stage.show();
    }

    public static class MemoryLeakTestStarter {

        public static void main(String[] args) {
            MemoryLeakTest.main(args);
        }

    }

}
