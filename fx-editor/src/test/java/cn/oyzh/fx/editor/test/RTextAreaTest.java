package cn.oyzh.fx.editor.test;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.pane.FXScrollPane;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rtextarea.LineNumberFormatter;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.InputStream;
import java.time.Year;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class RTextAreaTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXVBox vBox = new FXVBox();
        TextEditorPane editor = new TextEditorPane();
        editor.setLineWrap(true);
        // // // 放入滚动面板（避免内容溢出）
        RTextScrollPane swingScrollPane = new RTextScrollPane(editor);
        swingScrollPane.setLineNumbersEnabled(true);
        swingScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // JPanel jPanel = new JPanel();
        // jPanel.add(editor);
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> {
            // editor.setSize(800, 500);
            // editor.setPreferredSize(new Dimension(0,0));
            // editor.setMinimumSize(new java.awt.Dimension(800, 500));
            // swingScrollPane.setSize(800, 500);
            // swingScrollPane.setPreferredSize(new Dimension(0,0));
            // swingScrollPane.setMinimumSize(new java.awt.Dimension(800, 500));
            swingNode.setContent(swingScrollPane);
            // swingNode.setContent(jPanel);
            // swingNode.setContent(editor);


        });
        editor.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                swingNode.minWidth(e.getComponent().getWidth());
                swingNode.minHeight(e.getComponent().getHeight());
                int w = e.getComponent().getWidth();
                int h = e.getComponent().getHeight();
                System.out.println("w=" + w + ",h=" + h);
            }
        });

        swingNode.boundsInLocalProperty().addListener((obs, oldBounds, newBounds) -> {
            // 从新边界中获取宽度和高度
            double newWidth = newBounds.getWidth();
            double newHeight = newBounds.getHeight();
            System.out.println("尺寸变化：宽=" + newWidth + ", 高=" + newHeight);

            // 同步调整内部Swing组件的大小（线程安全）
            SwingUtilities.invokeLater(() -> {
                if (swingNode.getContent() != null) {
                    // 设置Swing组件大小与SwingNode一致
                    swingNode.getContent().setSize((int) newWidth, (int) newHeight);
                    swingNode.getContent().revalidate(); // 刷新布局
                }
            });
        });


        swingNode.prefHeight(500);
        swingNode.minHeight(500);
        swingNode.prefWidth(800);
        swingNode.minWidth(800);


        // 3. 创建JavaFX原生ScrollPane，并将SwingNode作为内容
        FXScrollPane fxScrollPane = new FXScrollPane();
        fxScrollPane.setContent(swingNode);
        // fxScrollPane.setPrefWidth(800);
        fxScrollPane.setPrefHeight(500);

        // 配置ScrollPane属性（关键）
        fxScrollPane.setFitToWidth(true); // 内容宽度适配ScrollPane
        fxScrollPane.setFitToHeight(true); // 高度不强制适配（按内容高度滚动）
        fxScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // 横向滚动按需显示
        fxScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // 纵向滚动按需显示

        // vBox.addChild(swingNode);
        vBox.addChild(fxScrollPane);

        FXHBox hBox1 = new FXHBox();
        javafx.scene.control.Button btn_1 = new javafx.scene.control.Button("json数据");
        btn_1.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.json");
            byte[] bytes = IOUtil.readBytes(stream);
            SwingUtilities.invokeLater(() -> {
                editor.setText(new String(bytes));
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            });
        });
        hBox1.addChild(btn_1);

        javafx.scene.control.Button btn_2 = new javafx.scene.control.Button("html数据");
        btn_2.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.html");
            String string = IOUtil.readDefaultString(stream);
            SwingUtilities.invokeLater(() -> {
                editor.setText(string);
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
            });
        });
        hBox1.addChild(btn_2);

        javafx.scene.control.Button btn_3 = new javafx.scene.control.Button("xml数据");
        btn_3.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.xml");
            String string = IOUtil.readDefaultString(stream);
            SwingUtilities.invokeLater(() -> {
                editor.setText(string);
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
            });
        });
        hBox1.addChild(btn_3);

        javafx.scene.control.Button btn_4 = new javafx.scene.control.Button("yaml数据");
        btn_4.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.yaml");
            String string = IOUtil.readDefaultString(stream);
            SwingUtilities.invokeLater(() -> {
                editor.setText(string);
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_YAML);
            });
        });
        hBox1.addChild(btn_4);

        javafx.scene.control.Button btn_5 = new javafx.scene.control.Button("css数据");
        btn_5.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.css");
            String string = IOUtil.readDefaultString(stream);
            SwingUtilities.invokeLater(() -> {
                editor.setText(string);
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSS);
            });
        });
        hBox1.addChild(btn_5);

        javafx.scene.control.Button btn_6 = new Button("properties数据");
        btn_6.setOnAction(actionEvent -> {
            InputStream stream = ResourceUtil.getResourceAsStream("test.properties");
            String string = IOUtil.readUtf8String(stream);
            SwingUtilities.invokeLater(() -> {
                editor.setText(string);
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
            });
        });
        hBox1.addChild(btn_6);

        // vBox.addChild(swingNode);
        // // vBox.addChild(fxScrollPane);
        vBox.addChild(hBox1);


        FXHBox hBox2 = new FXHBox();
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
        vBox.addChild(hBox2);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }

    public static class RTextAreaStarter {
        public static void main(String[] args) {
            RTextAreaTest.main(args);
        }
    }
}
