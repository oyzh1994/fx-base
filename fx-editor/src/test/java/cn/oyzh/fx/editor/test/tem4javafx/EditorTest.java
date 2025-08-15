package cn.oyzh.fx.editor.test.tem4javafx;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.editor.EditorLineNumPolicy;
import cn.oyzh.fx.editor.tm4javafx.Editor;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatType;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatTypeComboBox;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.font.FontFamilyComboBox;
import cn.oyzh.fx.plus.font.FontSizeComboBox;
import cn.oyzh.fx.plus.font.FontWeightComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.theme.ThemeComboBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class EditorTest extends Application {

    public static void main(String[] args) {
        launch(EditorTest.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // ThemeManager.apply(Themes.PRIMER_LIGHT);
        ThemeManager.apply(Themes.PRIMER_DARK);
        test1(stage);
        stage.setTitle("编辑器测试");
    }

    private void test1(Stage stage) {
        FXVBox vBox = new FXVBox();
        vBox.setFlexWidth("100%");
        vBox.setFlexHeight("100%");

        Editor editor = new Editor();

        editor.setFlexWidth("100%");
        editor.setFlexHeight("100% - 120");
        editor.setLineNumPolicy(EditorLineNumPolicy.ALWAYS);

        FXHBox hBox = new FXHBox();

        // 高亮
        FXTextField text_31 = new FXTextField();
        text_31.setPromptText("查找内容");
        editor.highlightTextProperty().bind(text_31.textProperty());
        hBox.addChild(text_31);

        EditorFormatTypeComboBox comboBox = new EditorFormatTypeComboBox();

        comboBox.selectedItemChanged((observableValue, s, t1) -> {
            if (t1 != null) {
                try {
                    String name = "/tm4javafx/example/" + t1.getSyntaxesName() + ".example." + t1.getExtension();
                    InputStream stream = ResourceUtil.getResourceAsStream(name);
                    String data = IOUtil.readDefaultString(stream);
                    editor.showData(data, t1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        hBox.addChild(new FXLabel("数据"));
        hBox.addChild(comboBox);

        // 提示词
        FXTextField text_32 = new FXTextField();
        text_32.setPromptText("提示词");
        text_32.addTextChangeListener((observableValue, s, t1) -> {
            if (t1.contains(",")) {
                editor.setPrompts(Set.of(t1.split(",")));
            } else if (StringUtil.isBlank(t1)) {
                editor.setPrompts(Collections.emptySet());
            } else {
                editor.setPrompts(Set.of(t1));
            }
        });
        hBox.addChild(text_32);
        // 格式
        EditorFormatTypeComboBox formatTypeComboBox = new EditorFormatTypeComboBox();
        formatTypeComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
            editor.setFormatType(t1);
        });
        editor.formatTypeProperty().addListener((observableValue, formatType, t1) -> {
            formatTypeComboBox.setValue(t1);
        });
        hBox.addChild(new FXLabel("内容格式"));
        hBox.addChild(formatTypeComboBox);

        FXHBox hBox2 = new FXHBox();
        ThemeComboBox themeComboBox = new ThemeComboBox();
        themeComboBox.selectedItemChanged((observableValue, themeStyle, t1) -> {
            ThemeManager.apply(t1);
        });
        hBox2.addChild(new FXLabel("主题"));
        hBox2.addChild(themeComboBox);
        // 字体大小
        FontSizeComboBox fontSizeComboBox = new FontSizeComboBox();
        fontSizeComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
            editor.setFontSize(t1);
        });
        hBox2.addChild(new FXLabel("字体大小"));
        hBox2.addChild(fontSizeComboBox);
        // 字体名称
        FontFamilyComboBox fontFamilyComboBox = new FontFamilyComboBox();
        fontFamilyComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
            editor.setFontFamily(t1);
        });
        hBox2.addChild(new FXLabel("字体名称"));
        hBox2.addChild(fontFamilyComboBox);
        // 字体字重
        FontWeightComboBox fontWeightComboBox = new FontWeightComboBox();
        fontWeightComboBox.selectedItemChanged((observableValue, formatType, t1) -> {
            editor.setFontWeight(t1);
        });
        hBox2.addChild(new FXLabel("字体字重"));
        hBox2.addChild(fontWeightComboBox);

        FXHBox hBox3 = new FXHBox();
        // 替换
        Button btn_31 = new Button("替换");
        btn_31.setOnAction(event -> {
            editor.replaceText(0, 4, "test");
        });
        hBox3.addChild(btn_31);
        // 追加
        Button btn_32 = new Button("追加");
        btn_32.setOnAction(event -> {
            editor.appendText("test1");
        });
        hBox3.addChild(btn_32);
        // 追加
        Button btn_33 = new Button("追加行");
        btn_33.setOnAction(event -> {
            editor.appendLine("test2");
        });
        hBox3.addChild(btn_33);
        // 清除
        Button btn_34 = new Button("清除");
        btn_34.setOnAction(event -> {
            editor.clear();
        });
        hBox3.addChild(btn_34);
        Button btn_35 = new Button("复制");
        btn_35.setOnAction(event -> {
            editor.copy();
        });
        hBox3.addChild(btn_35);
        Button btn_36 = new Button("粘贴");
        btn_36.setOnAction(event -> {
            editor.paste();
        });
        hBox3.addChild(btn_36);
        Button btn_37 = new Button("重做");
        btn_37.setOnAction(event -> {
            editor.redo();
        });
        hBox3.addChild(btn_37);
        Button btn_38 = new Button("撤销");
        btn_38.setOnAction(event -> {
            editor.undo();
        });
        hBox3.addChild(btn_38);
        Button btn_39 = new Button("光标到末尾");
        btn_39.setOnAction(event -> {
            editor.moveCaretEnd();
        });
        hBox3.addChild(btn_39);
        Button btn_310 = new Button("光标到开始");
        btn_310.setOnAction(event -> {
            editor.moveCaretStart();
        });
        hBox3.addChild(btn_310);
        Button btn_311 = new Button("光标到中间");
        btn_311.setOnAction(event -> {
            editor.positionCaret(editor.getLength() / 2);
        });
        hBox3.addChild(btn_311);
        // Button btn_312 = new Button("设置颜色");
        // btn_312.setOnAction(event -> {
        //     editor.setStyle(0, 10, Color.RED);
        // });
        // hBox3.addChild(btn_312);

        FXHBox hBox4 = new FXHBox();
        Button btn_41 = new Button("获取光标位置");
        btn_41.setOnAction(event -> {
            MessageBox.info(editor.caretPosition() + "");
        });
        hBox4.addChild(btn_41);
        Button btn_42 = new Button("获取光标边界");
        btn_42.setOnAction(event -> {
            MessageBox.info(editor.getCaretBounds() + "");
        });
        hBox4.addChild(btn_42);
        Button btn_43 = new Button("在光标处弹窗");
        btn_43.setOnAction(event -> {
            Popup popup = new Popup();
            popup.setHeight(100);
            popup.setWidth(200);
            popup.getContent().setAll(new FXButton("test1"));
            Optional<Bounds> bounds = editor.getCaretBounds();
            double x = bounds.get().getCenterX();
            double y = bounds.get().getCenterY();
            popup.show(editor, x, y);
            FXUtil.runLater(popup::hide, 1500);
        });
        hBox4.addChild(btn_43);


        vBox.addChild(hBox);
        vBox.addChild(hBox2);
        vBox.addChild(hBox3);
        vBox.addChild(hBox4);
        vBox.addChild(editor);
        // vBox.addChild(editor1);


        Scene scene = new Scene(vBox);

        stage.setWidth(800);
        stage.setHeight(600);

        stage.setScene(scene);
        stage.show();
    }

    public static class EditorTestStarter {

        public static void main(String[] args) {
            EditorTest.main(args);
        }

    }

}
