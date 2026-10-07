//package cn.oyzh.fx.editor.test;
//
//import org.fife.ui.rsyntaxtextarea.TextEditorPane;
//
//import javax.swing.JFrame;
//import javax.swing.SwingUtilities;
//import java.lang.reflect.InvocationTargetException;
//
///**
// * Swing 编辑器基础测试（已注释），在 JFrame 中展示 RSyntaxTextArea 的 TextEditorPane。
// *
// * @author oyzh
// * @since 2025-08-11
// */
//public class SwingTest extends JFrame {
//
//    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
//        SwingTest frame = new SwingTest();
//        TextEditorPane pane = new TextEditorPane();
//        SwingUtilities.invokeAndWait(() -> {
//            pane.setText("Hello World");
//        });
//        frame.add(pane);
//        frame.setVisible(true);
//        frame.setSize(400, 300);
//    }
//}
