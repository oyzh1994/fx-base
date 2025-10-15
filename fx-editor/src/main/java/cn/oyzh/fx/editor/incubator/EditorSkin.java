package cn.oyzh.fx.editor.incubator;

import cn.oyzh.fx.plus.theme.ThemeManager;
import com.sun.jfx.incubator.scene.control.richtext.CaretInfo;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
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
        Path path1 = this.getCaretPath();
        // 监听光标，防止颜色被修改
        path1.fillProperty().addListener((observableValue, paint, t1) -> {
            Color color = this.caretColor;
            if (color != null && t1 != color) {
                path1.setFill(color);
            }
        });
        Path path2 = this.getCaretLineHighlight();
        // 监听高亮行，防止颜色被修改
        path2.fillProperty().addListener((observableValue, paint, t1) -> {
            Color color = this.caretLineColor;
            if (color != null && t1 != color) {
                path2.setFill(color);
            }
        });
        Path path3 = this.getSelectionHighlight();
        // 监听选区，防止颜色被修改
        path3.fillProperty().addListener((observableValue, paint, t1) -> {
            Color color = this.selectionColor;
            if (color != null && t1 != color) {
                this.setSelectionColor(color);
            }
        });
    }

    public VFlow getVFlow() {
        for (Node child : this.getChildren()) {
            if (child instanceof VFlow flow) {
                return flow;
            }
        }
        return null;
    }

    /**
     * 获取光标信息
     *
     * @return 光标信息
     */
    public CaretInfo getCaretInfo() {
        VFlow vFlow = this.getVFlow();
        return vFlow == null ? null : vFlow.getCaretInfo();
    }

    /**
     * 获取光标组件
     *
     * @return 组件
     */
    public Path getCaretPath() {
        VFlow vFlow = this.getVFlow();
        return (Path) vFlow.lookup("Path.caret");
        // Node node = vFlow.lookup("Path.caret");
        // if (node instanceof Path path) {
        //     return path;
        // }
        // return ReflectUtil.getFieldValue(vFlow, "caretPath");
    }

    /**
     * 获取选区高亮组件
     *
     * @return 组件
     */
    public Path getSelectionHighlight() {
        VFlow vFlow = this.getVFlow();
        return (Path) vFlow.lookup("Path.selection-highlight");
        // Node node = vFlow.lookup("Path.selection-highlight");
        // if (node instanceof Path path) {
        //     return path;
        // }
        // return ReflectUtil.getFieldValue(vFlow, "selectionHighlight");
    }

    /**
     * 获取光标行组件
     *
     * @return 组件
     */
    public Path getCaretLineHighlight() {
        VFlow vFlow = this.getVFlow();
        return (Path) vFlow.lookup("Path.caret-line");
        // Node node = vFlow.lookup("Path.caret-line");
        // if (node instanceof Path path) {
        //     return path;
        // }
        // return ReflectUtil.getFieldValue(vFlow, "caretLineHighlight");
    }

    /**
     * 光标颜色
     */
    private Color caretColor;

    /**
     * 设置光标颜色
     *
     * @param color 光标颜色
     */
    public void setCaretColor(Color color) {
        this.caretColor = color;
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
        if (this.caretColor != null) {
            return this.caretColor;
        }
        Path caretPath = this.getCaretPath();
        return caretPath == null ? null : (Color) caretPath.getStroke();
    }

    /**
     * 光标行颜色
     */
    private Color caretLineColor;

    /**
     * 设置光标行颜色
     *
     * @param color 光标行颜色
     */
    public void setCaretLineColor(Color color) {
        this.caretLineColor = color;
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
        if (this.caretLineColor != null) {
            return this.caretLineColor;
        }
        Path caretPath = this.getCaretLineHighlight();
        return caretPath == null ? null : (Color) caretPath.getStroke();
    }

    /**
     * 选区颜色
     */
    private Color selectionColor;

    /**
     * 设置选区颜色
     *
     * @param color 选区颜色
     */
    public void setSelectionColor(Color color) {
        this.selectionColor = color;
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
        if (this.selectionColor != null) {
            return this.selectionColor;
        }
        Path caretPath = this.getSelectionHighlight();
        return caretPath == null ? null : (Color) caretPath.getStroke();
    }
}
