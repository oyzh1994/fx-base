package cn.oyzh.fx.plus.test;

import atlantafx.base.controls.Spin;
import atlantafx.base.theme.Theme;
import atlantafx.base.theme.ThemeManager;
import atlantafx.spins.ClockSpin;
import atlantafx.spins.DoubleArcSpin;
import atlantafx.spins.GearsSpin;
import atlantafx.spins.TextFillSpin;
import atlantafx.spins.TextProgressSpin;
import cn.oyzh.fx.plus.ext.FXApplication;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author oyzh
 * @since 2026-09-24
 */
public class AtlantafxSpinsTest extends FXApplication {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        test1(primaryStage);
        ThemeManager.instance().setTheme(Themes.SYSTEM);
    }

    private void test1(Stage stage) {
        VBox vBox = new VBox();
//        vBox.getChildren().add(GearsSpin.create());
//        vBox.getChildren().add(ClockSpin.create());
        vBox.getChildren().add(DoubleArcSpin.create());
        vBox.getChildren().add(TextFillSpin.create());
        vBox.getChildren().add(TextProgressSpin.create());
        stage.setScene(new Scene(vBox));
        stage.show();
    }

    public static class AtlantafxSpinsStarter {
        public static void main(String[] args) {
            AtlantafxSpinsTest.main(args);
        }
    }
}
