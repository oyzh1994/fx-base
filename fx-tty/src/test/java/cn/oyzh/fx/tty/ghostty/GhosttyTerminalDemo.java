package cn.oyzh.fx.tty.ghostty;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 手动体验 Ghostty 终端的 JavaFX 示例。
 *
 * <p>在 IDE 中直接运行本类的 {@link #main(String[])} 即可；请确保 VM options 包含
 * {@code --enable-native-access=ALL-UNNAMED}。</p>
 */
public final class GhosttyTerminalDemo extends Application {

    @Override
    public void start(Stage stage) {
        TerminalView view = GhosttyTerminals.localShell();
        var root = new BorderPane(view);
        var scene = new Scene(root, 1000, 700);
        stage.titleProperty().bind(view.titleProperty());
        stage.setScene(scene);
        stage.setOnCloseRequest(_ -> view.close());
        stage.show();
        view.setTheme(TerminalTheme.defaults());
        view.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
