package cn.oyzh.fx.editor.test;

import org.fife.ui.rsyntaxtextarea.TextEditorPane;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

/**
 * @author oyzh
 * @since 2025-08-11
 */
public class SwingTest extends JFrame {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        SwingTest frame = new SwingTest();
        TextEditorPane pane = new TextEditorPane();
        SwingUtilities.invokeAndWait(() -> {
            pane.setText("Hello World");
        });
        frame.add(pane);
        frame.setVisible(true);
        frame.setSize(400, 300);
    }
}
