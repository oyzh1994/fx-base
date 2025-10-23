package cn.oyzh.fx.rich.richtextfx;

import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.function.IntFunction;

/**
 * @author oyzh
 * @since 2025-03-25
 */
public class RichLineNumberFactory implements IntFunction<Node> {

    private final LineNumberFactory<Node> factory;

    public RichLineNumberFactory(GenericStyledArea<?, ?, ?> area) {
        this.factory = (LineNumberFactory<Node>) LineNumberFactory.get(area);
    }

    @Override
    public Node apply(int value) {
        Node node = this.factory.apply(value);
        if (node instanceof Label label) {
            label.setTextFill(ThemeManager.currentAccentColor());
            label.setBackground(ControlUtil.background(ThemeManager.currentBackgroundColor()));
        }
        return node;
    }
}
