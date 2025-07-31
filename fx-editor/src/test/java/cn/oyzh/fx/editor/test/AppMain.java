package cn.oyzh.fx.editor.test;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.fx.editor.Editor;
import cn.oyzh.fx.editor.EditorFormatType;
import cn.oyzh.fx.editor.EditorFormatTypeComboBox;
import cn.oyzh.fx.editor.EditorLineNumPolicy;
import cn.oyzh.fx.editor.EditorPane;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.TwoDimensional;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;


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
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
        test1(stage);
    }

    private void test1(Stage stage) {
        FXVBox vBox = new FXVBox();
        vBox.setFlexWidth("100%");
        vBox.setFlexHeight("100%");


        EditorPane editor = new EditorPane();
        // Editor editor = new Editor();
        editor.setFlexWidth("100%");
        editor.setFlexHeight("100% - 60");
        editor.setLineNumPolicy(EditorLineNumPolicy.ALWAYS);


        // editor.getContent().setAutoScrollOnDragDesired(false);

        AtomicBoolean ignore = new AtomicBoolean(false);
        // editor.getContent().estimatedScrollYProperty().addListener((observableValue, aDouble, t1) -> {
        //     // if(editor.isIgnoreVChanged()){
        //     //     return;
        //     // }
        //     //  System.out.println(t1);
        //     //  // editor.initTextStyle();
        //     //  if (ignore.get()) {
        //     //      ignore.set(false);
        //     //  } else {
        //     //      ignore.set(true);
        //     //      editor.initTextStyle();
        //     //  }
        //     System.out.println(t1);
        // });

        // editor.addEventFilter(ScrollEvent.SCROLL, e -> {
        //     System.out.println("----1");
        //     editor.initTextStyle();
        // });
        //
        // editor.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
        //     if (e.getCode() == KeyCode.UP
        //             || e.getCode() == KeyCode.DOWN) {
        //         System.out.println("----2");
        //         editor.initTextStyle();
        //     } else if (e.getCode() == KeyCode.PAGE_UP
        //             || e.getCode() == KeyCode.PAGE_DOWN) {
        //         System.out.println("----3");
        //         editor.initTextStyleDelay();
        //     }
        // });

        // editor.getContent().getVisibleParagraphs().addListener((ListChangeListener<Paragraph<String, String, String>>) change -> {
        //     if (ignore.get()) {
        //         ignore.set(false);
        //     } else {
        //         ignore.set(true);
        //         editor.initTextStyle();
        //     }
        // });


        // editor.getContent().getVisibleParagraphs().addListener((ListChangeListener<Paragraph<String, String, String>>) change -> {
        //
        //     System.out.println("--------------1");
        //     // for (Paragraph<String, String, String> visibleParagraph : editor.getContent().getVisibleParagraphs()) {
        //     //
        //     //     System.out.println(visibleParagraph.getText() );
        //     //
        //     //     visibleParagraph.getStyleSpans().iterator().next().
        //     // }
        //     try {
        //         TwoDimensional.Position index1 = editor.getContent().offsetToPosition(0, TwoDimensional.Bias.Backward);
        //         TwoDimensional.Position index2 = editor.getContent().offsetToPosition(100, TwoDimensional.Bias.Backward);
        //         if (index1 != null && index2 != null) {
        //             System.out.println(index1.getMajor() + "=" + index2.getMajor());
        //         }
        //     }catch (Exception ex){
        //
        //     }
        //     System.out.println("--------------2");
        // });


        FXHBox hBox = new FXHBox();

        FXTextField textField = new FXTextField();
        // textField.addTextChangeListener((observableValue, s, t1) -> {
        //     editor.setHighlightText(t1);
        // });
        editor.highlightTextProperty().bind(textField.textProperty());
        hBox.addChild(textField);

        EditorFormatTypeComboBox formatTypeComboBox = new EditorFormatTypeComboBox();
        // editor.formatTypeProperty().bind(formatTypeComboBox.selectedItemProperty());

        formatTypeComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
            editor.setFormatType(t1);
        });
        hBox.addChild(formatTypeComboBox);

        Button btn_1 = new Button("json数据");
        btn_1.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.json");
            byte[] bytes = IOUtil.readBytes(stream);
            editor.showJsonData(new String(bytes));
        });
        hBox.addChild(btn_1);

        Button btn_2 = new Button("html数据");
        btn_2.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.html");
            String string = IOUtil.readDefaultString(stream);
            editor.showHtmlData(string);
        });
        hBox.addChild(btn_2);

        Button btn_3 = new Button("xml数据");
        btn_3.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.xml");
            String string = IOUtil.readDefaultString(stream);
            editor.showXmlData(string);
        });
        hBox.addChild(btn_3);

        Button btn_4 = new Button("yaml数据");
        btn_4.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.yaml");
            String string = IOUtil.readDefaultString(stream);
            editor.showYamlData(string);
        });
        hBox.addChild(btn_4);

        FXHBox hBox2=new FXHBox();

        Button btn_21 = new Button("明亮主题");
        btn_21.setOnAction(actionEvent -> {
            // Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
            ThemeManager.apply(Themes.PRIMER_LIGHT);
        });
        Button btn_22 = new Button("黑暗主题");
        btn_22.setOnAction(actionEvent -> {
            // Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
            ThemeManager.apply(Themes.DRACULA);
        });
        hBox2.addChild(btn_21);
        hBox2.addChild(btn_22);

        vBox.addChild(hBox);
        vBox.addChild(hBox2);
        vBox.addChild(editor);

        Scene scene = new Scene(vBox);

        stage.setWidth(800);
        stage.setHeight(600);

        stage.setScene(scene);
        stage.show();
    }


}
