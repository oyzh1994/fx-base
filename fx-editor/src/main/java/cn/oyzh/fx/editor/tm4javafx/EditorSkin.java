package cn.oyzh.fx.editor.tm4javafx;

import cn.oyzh.common.util.ReflectUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import com.sun.jfx.incubator.scene.control.richtext.CaretInfo;
import com.sun.jfx.incubator.scene.control.richtext.ClippedPane;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeType;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.skin.CodeAreaSkin;

/**
 * @author oyzh
 * @since 2025-08-14
 */
public class EditorSkin extends CodeAreaSkin {

    public EditorSkin(Editor control) {
        super(control);
        this.setCaretColor(ThemeManager.currentAccentColor());
        this.setCaretLineColor(control.defaultCaretLineColor());
        this.setSelectionColor(control.defaultSelectionColor());
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

    public Path getSelectionHighlight() {
        VFlow vFlow = this.getVFlow();
        return ReflectUtil.getFieldValue(vFlow, "selectionHighlight");
    }

    public Path getCaretLineHighlight() {
        VFlow vFlow = this.getVFlow();
        return ReflectUtil.getFieldValue(vFlow, "caretLineHighlight");
    }

    public ClippedPane getCaretLineHighlight1() {
        VFlow vFlow = this.getVFlow();
        return ReflectUtil.getFieldValue(vFlow, "rightGutter");
    }

    /**
     * 设置光标颜色
     *
     * @param color 光标颜色
     */
    public void setCaretColor(Color color) {
        Path caretPath = this.getCaretPath();
        if (caretPath != null) {
            caretPath.setFill(color);
            caretPath.setStroke(color);
        }
    }

    /**
     * 获取光标颜色
     *
     * @return 光标颜色
     */
    public Color getCaretColor() {
        Path caretPath = this.getCaretPath();
        return caretPath == null ? null : (Color) caretPath.getStroke();
    }

    /**
     * 设置光标行颜色
     *
     * @param color 光标行颜色
     */
    public void setCaretLineColor(Color color) {
        Path caretPath = this.getCaretLineHighlight();
        if (caretPath != null) {
            caretPath.setFill(color);
            caretPath.setStroke(color);
        }
    }

    /**
     * 获取光标行颜色
     *
     * @return 光标行颜色
     */
    public Color getCaretLineColor() {
        Path caretPath = this.getCaretLineHighlight();
        return caretPath == null ? null : (Color) caretPath.getStroke();
    }

    /**
     * 设置选区颜色
     *
     * @param color 选区颜色
     */
    public void setSelectionColor(Color color) {
        Path caretPath = this.getSelectionHighlight();
        if (caretPath != null) {
            caretPath.setFill(color);
            caretPath.setStroke(color);
        }
    }

    /**
     * 获取选区颜色
     *
     * @return 选区颜色
     */
    public Color getSelectionColor() {
        Path caretPath = this.getSelectionHighlight();
        return caretPath == null ? null : (Color) caretPath.getStroke();
    }
}
