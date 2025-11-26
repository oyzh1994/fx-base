package cn.oyzh.fx.gui.test;

import atlantafx.base.controls.Breadcrumbs;
import atlantafx.base.controls.Calendar;
import atlantafx.base.controls.Card;
import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.MaskTextField;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.SegmentedControl;
import atlantafx.base.controls.Tab;
import atlantafx.base.controls.TabLine;
import atlantafx.base.controls.ToggleLabel;
import atlantafx.base.layout.DeckPane;
import atlantafx.base.layout.InputGroup;
import atlantafx.base.layout.ModalBox;
import cn.oyzh.fx.gui.svg.glyph.CloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.OpenSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SaveSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author oyzh
 * @since 2025-11-14
 */
public class AtlantafxTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        // test1(stage);
        // test2(stage);
        // test3(stage);
        // test4(stage);
        // test5(stage);
        // test6(stage);
        // test7(stage);
        // test8(stage);
        // test9(stage);
        // test10(stage);
        test11(stage);
    }

    private void test1(Stage stage) {
        CustomTextField customTextField = new CustomTextField();
        customTextField.setLeft(new SaveSVGGlyph());
        customTextField.setRight(new CopySVGGlyph());
        FXVBox fxVBox = new FXVBox(customTextField);
        Scene scene = new Scene(fxVBox);
        stage.setScene(scene);
        stage.show();
    }

    private void test2(Stage stage) {
        Breadcrumbs.BreadCrumbItem<String> item1 = new Breadcrumbs.BreadCrumbItem<>("aaa");
        Breadcrumbs.BreadCrumbItem<String> sub1 = new Breadcrumbs.BreadCrumbItem<>("sub1");
        Breadcrumbs.BreadCrumbItem<String> sub2 = new Breadcrumbs.BreadCrumbItem<>("sub2");
        Breadcrumbs.BreadCrumbItem<String> sub3 = new Breadcrumbs.BreadCrumbItem<>("sub3");
        item1.getChildren().add(sub1);
        sub1.getChildren().add(sub2);
        sub2.getChildren().add(sub3);
        Breadcrumbs<String> breadcrumbs = new Breadcrumbs<>(sub3);
        FXVBox fxVBox = new FXVBox(breadcrumbs);
        Scene scene = new Scene(fxVBox);
        stage.setScene(scene);
        stage.show();
    }

    private void test3(Stage stage) {
        Calendar calendar = new Calendar();
        FXVBox fxVBox = new FXVBox(calendar);
        Scene scene = new Scene(fxVBox);
        stage.setScene(scene);
        stage.show();
    }

    private void test4(Stage stage) {
        Card card = new Card();
        card.setHeader(new CopySVGGlyph());
        card.setSubHeader(new OpenSVGGlyph());
        card.setFooter(new CloseSVGGlyph());
        card.setBody(new SaveSVGGlyph());
        FXVBox fxVBox = new FXVBox(card);
        Scene scene = new Scene(fxVBox);
        stage.setScene(scene);
        stage.show();
    }

    private void test5(Stage stage) {
        MaskTextField textField = new MaskTextField();
        textField.setMask("######");
        FXVBox fxVBox = new FXVBox(textField);
        Scene scene = new Scene(fxVBox);
        stage.setScene(scene);
        stage.show();
    }

    private void test6(Stage stage) {
        ModalPane modalPane = new ModalPane();
        modalPane.setContent(new SaveSVGGlyph());
        modalPane.setPersistent(false);
        modalPane.setDisplay(false);
        Scene scene = new Scene(modalPane);
        stage.setScene(scene);
        stage.show();
    }

    private void test7(Stage stage) {
        SegmentedControl control = new SegmentedControl();
        control.getSegments().add(new ToggleLabel("aaa"));
        control.getSegments().add(new ToggleLabel("bbb"));
        control.getSegments().add(new ToggleLabel("ccc"));
        Scene scene = new Scene(control);
        stage.setScene(scene);
        stage.show();
    }

    private void test8(Stage stage) {
        TabLine control = new TabLine();
        control.getTabs().add(new Tab("name"));
        control.getTabs().add(new Tab("age"));
        Scene scene = new Scene(control);
        stage.setScene(scene);
        stage.show();
    }

    private void test9(Stage stage) {
        DeckPane pane = new DeckPane();
        pane.getChildren().add(new SaveSVGGlyph());
        pane.getChildren().add(new OpenSVGGlyph());
        pane.getChildren().add(new CloseSVGGlyph());
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    private void test10(Stage stage) {
        InputGroup pane = new InputGroup();
        pane.getChildren().add(new SaveSVGGlyph());
        pane.getChildren().add(new OpenSVGGlyph());
        pane.getChildren().add(new CloseSVGGlyph());
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    private void test11(Stage stage) {
        ModalBox pane = new ModalBox();
        pane.getChildren().add(new SaveSVGGlyph());
        pane.getChildren().add(new OpenSVGGlyph());
        pane.getChildren().add(new CloseSVGGlyph());
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    public static class AtlantafxTestStarter {

        public static void main(String[] args) {
            AtlantafxTest.main(args);
        }
    }
}
