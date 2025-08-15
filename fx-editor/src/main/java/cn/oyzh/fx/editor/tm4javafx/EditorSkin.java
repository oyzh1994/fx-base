package cn.oyzh.fx.editor.tm4javafx;

import cn.oyzh.common.util.ReflectUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import com.sun.jfx.incubator.scene.control.richtext.CaretInfo;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.skin.CodeAreaSkin;

/**
 * @author oyzh
 * @since 2025-08-14
 */
public class EditorSkin extends CodeAreaSkin {

    public EditorSkin(CodeArea control) {
        super(control);
        this.setCartColor(ThemeManager.currentAccentColor());
    }

    public VFlow getVFlow() {
        for (Node child : this.getChildren()) {
            if (child instanceof VFlow flow) {
                return flow;
            }
        }
        return null;
    }

    public CaretInfo getCaretInfo() {
        VFlow vFlow = this.getVFlow();
        return vFlow == null ? null : vFlow.getCaretInfo();
    }

    public Path getCaretPath() {
        VFlow vFlow = this.getVFlow();
        return ReflectUtil.getFieldValue(vFlow, "caretPath");
    }

    /**
     * 设置光标颜色
     *
     * @param color 颜色
     */
    public void setCartColor(Color color) {
        Path caretPath = this.getCaretPath();
        if (caretPath != null) {
            caretPath.setStroke(color);
        }
    }

    /**
     * 获取光标颜色
     *
     * @return 光标颜色
     */
    public Color getCartColor() {
        Path caretPath = this.getCaretPath();
        return caretPath == null ? null : (Color) caretPath.getStroke();
    }
}
