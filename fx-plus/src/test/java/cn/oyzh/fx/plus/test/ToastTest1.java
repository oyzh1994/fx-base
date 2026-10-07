package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.information.MessageBox;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * 浮动提示（Toast）测试，验证消息提示显示
 *
 * @author oyzh
 * @since 2024-11-15
 */
public class ToastTest1 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MessageBox.okToast("xx123456", primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class ToastTest1Main {

    public static void main(String[] args) {
        ToastTest1.main(args);
    }
}
