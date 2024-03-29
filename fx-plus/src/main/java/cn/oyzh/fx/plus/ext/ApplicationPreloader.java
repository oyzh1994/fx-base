package cn.oyzh.fx.plus.ext;

import javafx.application.Preloader;
import javafx.stage.Stage;

/**
 * @author oyzh
 * @since 2024/3/29
 */
public class ApplicationPreloader extends Preloader {

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    @Override
    public boolean handleErrorNotification(ErrorNotification info) {
        info.getCause().printStackTrace();
        return super.handleErrorNotification(info);
    }
}
