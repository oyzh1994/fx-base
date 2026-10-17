package cn.oyzh.fx.plus.test.svg;

import cn.oyzh.fx.gui.svg.glyph.UploadDownloadSVGGlyph;
import cn.oyzh.fx.gui.svg.label.UploadDownloadSVGLabel;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 自定义标题栏测试，演示原生 HeaderBar 与窗口样式
 *
 * @author oyzh
 * @since 2025-08-18
 */
public class SVGTest extends Application {

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // 应用主题
        //ThemeManager.apply(Themes.DRACULA);
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        UploadDownloadSVGGlyph svgGlyph = new UploadDownloadSVGGlyph();
        UploadDownloadSVGGlyph1 svgGlyph1 = new UploadDownloadSVGGlyph1();
        UploadDownloadSVGLabel svgLabel = new UploadDownloadSVGLabel();
        FXHBox hBox = new FXHBox(svgGlyph, svgGlyph1, svgLabel);
        FXHBox.setMargin(svgGlyph, new Insets(0, 0, 0, 5));
        FXHBox.setMargin(svgGlyph1, new Insets(0, 0, 0, 5));
        FXHBox.setMargin(svgLabel, new Insets(0, 0, 0, 5));
        Scene scene = new Scene(hBox, (double) 800.0F, (double) 600.0F);
        stage.initStyle(StageStyle.EXTENDED);
        stage.setScene(scene);
        scene.setFill(ThemeManager.currentBackgroundColor());
        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            System.out.println(event.getTarget());
        });
        stage.show();
    }

    public static class SVGTestStarter {
        public static void main(String[] args) {
            SVGTest.main(args);
        }
    }
}
