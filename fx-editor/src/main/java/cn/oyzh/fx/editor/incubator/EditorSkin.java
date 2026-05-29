package cn.oyzh.fx.editor.incubator;

import cn.oyzh.fx.plus.theme.ThemeManager;
import com.sun.jfx.incubator.scene.control.richtext.CaretInfo;
import com.sun.jfx.incubator.scene.control.richtext.RichTextAreaSkinHelper;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import jfx.incubator.scene.control.richtext.skin.CodeAreaSkin;

/**
 * @author oyzh
 * @since 2025-08-14
 */
public class EditorSkin extends CodeAreaSkin {

    private Path path1;

    private Path path2;

    private Path path3;

    private ChangeListener<? super Paint> path1Listener = (observableValue, paint, t1) -> {
        Color color = this.caretColor;
        if (color != null && t1 != color) {
            path1.setFill(color);
        }
    };

    private ChangeListener<? super Paint> path2Listener = (observableValue, paint, t1) -> {
        Color color = this.caretLineColor;
        if (color != null && t1 != color) {
            path2.setFill(color);
        }
    };

    private ChangeListener<? super Paint> path3Listener = (observableValue, paint, t1) -> {
        Color color = this.selectionColor;
        if (color != null && t1 != color) {
            this.setSelectionColor(color);
        }
    };

    public EditorSkin(Editor control) {
        super(control);
        this.setCaretColor(ThemeManager.currentForegroundColor());
        this.setCaretLineColor(control.defaultCaretLineColor());
        this.setSelectionColor(control.defaultSelectionColor());
        // 监听光标，防止颜色被修改
        this.path1 = this.getCaretPath();
        this.path1.fillProperty().addListener(this.path1Listener);
        // 监听高亮行，防止颜色被修改
        this.path2 = this.getCaretLineHighlight();
        this.path2.fillProperty().addListener(this.path2Listener);
        // 监听选区，防止颜色被修改
        this.path3 = this.getSelectionHighlight();
        this.path3.fillProperty().addListener(this.path3Listener);
    }

    private VFlow vFlow;

    /**
     * 获取VFlow对象
     *
     * @return 结果
     */
    public VFlow getVFlow() {
        if (this.vFlow == null) {
            for (Node child : this.getChildren()) {
                if (child instanceof VFlow flow) {
                    this.vFlow = flow;
                    break;
                }
            }
            if (this.vFlow == null) {
                this.vFlow = RichTextAreaSkinHelper.getVFlow(this.getSkinnable());
            }
        }
        return this.vFlow;
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
    }

    /**
     * 获取选区高亮组件
     *
     * @return 组件
     */
    public Path getSelectionHighlight() {
        VFlow vFlow = this.getVFlow();
        return (Path) vFlow.lookup("Path.selection-highlight");
    }

    /**
     * 获取光标行组件
     *
     * @return 组件
     */
    public Path getCaretLineHighlight() {
        VFlow vFlow = this.getVFlow();
        return (Path) vFlow.lookup("Path.caret-line");
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

    @Override
    public void dispose() {
        if (this.path1 != null) {
            this.path1.fillProperty().removeListener(this.path1Listener);
            this.path1 = null;
            this.path1Listener = null;
        }
        if (this.path2 != null) {
            this.path2.fillProperty().removeListener(this.path2Listener);
            this.path2 = null;
            this.path2Listener = null;
        }
        if (this.path3 != null) {
            this.path3.fillProperty().removeListener(this.path3Listener);
            this.path3 = null;
            this.path3Listener = null;
        }
        super.dispose();
    }
}
