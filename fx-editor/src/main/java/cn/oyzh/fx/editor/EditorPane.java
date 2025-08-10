package cn.oyzh.fx.editor;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.controls.swing.FXSwingNode;
import cn.oyzh.fx.plus.swing.SwingUtil;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Optional;
import java.util.Set;

/**
 * 编辑器面板
 *
 * @author oyzh
 * @since 2025/07/30
 */
public class EditorPane extends FXSwingNode {

    /**
     * 编辑器
     */
    private Editor editor;

    /**
     * 滚动面板
     */
    private RTextScrollPane scrollPane;

    /**
     * 获取编辑器
     *
     * @return 编辑器
     */
    public Editor getEditor() {
        return this.editor;
    }

    /**
     * 获取滚动面板
     *
     * @return 滚动面板
     */
    public RTextScrollPane getScrollPane() {
        return this.scrollPane;
    }

    /**
     * 获取内容提示
     *
     * @return 内容提示
     */
    public Set<String> getPrompts() {
        if (this.getEditor() == null) {
            return null;
        }
        return this.getEditor().getPrompts();
    }

    /**
     * 设置内容提示
     *
     * @param prompts 内容提示
     */
    public void setPrompts(Set<String> prompts) {
        if (this.getEditor() != null) {
            this.getEditor().setPrompts(prompts);
        }
    }

    public void showData(Object data) {
        this.getEditor().showData(data);
    }

    public void showData(Object data, EditorFormatType formatType) {
        this.getEditor().showData(data, formatType);
    }

    public EditorFormatType showDetectData(Object rawData) {
        return this.getEditor().showDetectData(rawData);
    }

    @Override
    public void initNode() {
        // 初始化swing组件
        Editor editor = new Editor();
        editor.setLineWrap(true);
        editor.setMaximumSize(null);
        editor.setPreferredSize(null);
        // 滚动面板
        RTextScrollPane scrollPane = new RTextScrollPane(editor);
        scrollPane.setLineNumbersEnabled(true);
        // TODO: 这个组件作为隔离用，不然跟fx的组件交互可能出现各种异常
        JPanel jPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                // 强制使用 Swing 自身的双缓冲，避免 JavaFX 渲染干扰
                super.paintComponent(g);
                if (g instanceof Graphics2D) {
                    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
            }
        };
        // 设置组件
        jPanel.add(scrollPane, BorderLayout.CENTER);
        // 设置组件
        this.editor = editor;
        this.scrollPane = scrollPane;
        // 设置swing组件
        this.setContent(jPanel);
        // 调用父类
        super.initNode();
        // 尝试初始化提示词
        this.setPrompts(this.getPrompts());
        // 尝试初始化高亮
        this.setHighlightText(this.getHighlightText());
        // 行号策略变化事件
        this.lineNumPolicyProperty().addListener((observableValue, editorLineNumPolicy, t1) -> {
            if (t1 == EditorLineNumPolicy.NONE) {
                this.hideLineNum();
            } else if (t1 == EditorLineNumPolicy.ALWAYS) {
                this.showLineNum();
            }
        });
        // linux可能获取不到焦点，单独监听
        if (OSUtil.isLinux()) {
            this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                this.requestFocus();
            });
        }
    }

    /**
     * 隐藏行号
     */
    public void hideLineNum() {
        this.getScrollPane().setLineNumbersEnabled(false);
    }

    /**
     * 显示行号
     */
    public void showLineNum() {
        this.getScrollPane().setLineNumbersEnabled(true);
    }

    /**
     * 行号策略
     */
    private ObjectProperty<EditorLineNumPolicy> lineNumPolicyProperty;

    public EditorLineNumPolicy getLineNumPolicy() {
        return this.lineNumPolicyProperty == null ? EditorLineNumPolicy.ALWAYS : this.lineNumPolicyProperty.get();
    }

    public void setLineNumPolicy(EditorLineNumPolicy lineNumPolicy) {
        this.lineNumPolicyProperty().set(lineNumPolicy);
    }

    public ObjectProperty<EditorLineNumPolicy> lineNumPolicyProperty() {
        if (this.lineNumPolicyProperty == null) {
            this.lineNumPolicyProperty = new SimpleObjectProperty<>(EditorLineNumPolicy.ALWAYS);
        }
        return this.lineNumPolicyProperty;
    }

    public StringProperty highlightTextProperty() {
        return this.getEditor().highlightTextProperty();
    }

    public void setFormatType(EditorFormatType formatType) {
        this.getEditor().setFormatType(formatType);
    }

    public String getText() {
        return this.getEditor().getText();
    }

    public void paste() {
        this.getEditor().paste();
    }

    public void clear() {
        this.getEditor().clear();
    }

    public void setText(String text) {
        this.getEditor().setText(text);
    }

    public String getTextTrim() {
        return this.getEditor().getTextTrim();
    }

    public String getHighlightText() {
        if (this.getEditor() == null) {
            return null;
        }
        return this.getEditor().getHighlightText();
    }

    public void setHighlightText(String highlightText) {
        if (this.getEditor() != null) {
            this.getEditor().setHighlightText(highlightText);
        }
    }

    /**
     * 选中选区并转到选区
     *
     * @param start 开始
     * @param end   结束
     */
    public void selectRangeAndGoto(int start, int end) {
        // try {
        //     System.out.println("start:" + start + " end:" + end);
        //     // 选中选区
        //     this.getEditor().selectRange(start, end);
        //     // // 将文本位置转换为屏幕坐标矩形
        //     // Rectangle2D rect = this.getEditor().modelToView2D( this.getCaretPosition());
        //     // if (rect != null) {
        //     //     // 滚动到该矩形区域（确保选中内容可见）
        //     //     this.scrollRectToVisible(rect.getBounds());
        //     // }
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        // }
        this.selectRange(start, end);
    }

    // /**
    //  * 滚动到可视区
    //  *
    //  * @param rectangle 范围
    //  */
    // public void scrollRectToVisible(Rectangle rectangle) {
    //     SwingUtil.runWait(() -> this.getScrollPane().getViewport().scrollRectToVisible(rectangle));
    // }

    /**
     * 撤销
     */
    public void undo() {
        this.getEditor().undo();
    }

    /**
     * 重做
     */
    public void redo() {
        this.getEditor().redo();
    }

    /**
     * 忘记历史
     */
    public void forgetHistory() {
        this.getEditor().forgetHistory();
    }

    public LongProperty caretPositionProperty() {
        return this.getEditor().caretPositionProperty();
    }

    public BooleanProperty undoableProperty() {
        return this.getEditor().undoableProperty();
    }

    public BooleanProperty redoableProperty() {
        return this.getEditor().redoableProperty();
    }

    public BooleanProperty editableProperty() {
        return this.getEditor().editableProperty();
    }

    public void addTextChangeListener(ChangeListener<String> listener) {
        this.getEditor().addTextChangeListener(listener);
    }

    public void setPromptText(String promptText) {
    }

    public String getPromptText() {
        return null;
    }

    /**
     * 获取光标位置
     *
     * @return 光标位置
     */
    public int getCaretPosition() {
        return this.getEditor().getCaretPosition();
    }

    /**
     * 获取光标边界
     *
     * @return 光标边界
     */
    public Optional<Bounds> getCaretBounds() {
        return this.getEditor().getCaretBounds();
    }

    /**
     * 替换内容
     *
     * @param start   开始位置
     * @param end     结束位置
     * @param content 内容
     */
    public void replaceText(int start, int end, String content) {
        this.getEditor().replaceText(start, end, content);
    }

    /**
     * 追加内容
     *
     * @param content 内容
     */
    public void appendText(String content) {
        this.getEditor().appendText(content);
    }

    /**
     * 追加行
     *
     * @param content 内容
     */
    public void appendLine(String content) {
        this.getEditor().appendLine(content);
    }

    /**
     * 追加行
     *
     * @param content 内容
     * @param endLine 是否追加尾部换行符
     */
    public void appendLine(String content, boolean endLine) {
        this.getEditor().appendLine(content, endLine);
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return this.getEditor().isEmpty();
    }

    public ObjectProperty<EditorFormatType> formatTypeProperty() {
        return this.getEditor().formatTypeProperty();
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        super.changeTheme(style);
        RTextScrollPane scrollPane = this.getScrollPane();
        SwingUtil.applyTheme(scrollPane);
        Editor editor = this.getEditor();
        EditorUtil.applyTheme(editor);
    }

    /**
     * 设置可编辑
     *
     * @param editable 是否可编辑
     */
    public void setEditable(boolean editable) {
        this.getEditor().setEditable(editable);
    }

    /**
     * 是否可编辑
     *
     * @return 结果
     */
    public boolean isEditable() {
        return this.getEditor().isEditable();
    }

    @Override
    protected Component realComponent() {
        return this.getEditor();
    }

    /**
     * 滚动到顶部
     */
    public void scrollToTop() {
        JScrollPane scrollPane = this.getScrollPane();
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        SwingUtil.runLater(() -> {
            bar.setValue(bar.getMinimum());
        });
    }

    /**
     * 滚动到底部
     */
    public void scrollToEnd() {
        JScrollPane scrollPane = this.getScrollPane();
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        SwingUtil.runLater(() -> {
            bar.setValue(bar.getMaximum());
        });
    }

    /**
     * 获取内容长度
     *
     * @return 结果
     */
    public int getLength() {
        return this.getEditor().getLength();
    }

    /**
     * 剪切
     */
    public void cut() {
        this.getEditor().cut();
    }

    /**
     * 复制
     */
    public void copy() {
        this.getEditor().copy();
    }

    /**
     * 设置光标位置
     *
     * @param caretPosition 位置
     */
    public void positionCaret(int caretPosition) {
        this.getEditor().positionCaret(caretPosition);
        this.requestFocus();
    }

    /**
     * 移动光标到末尾
     */
    public void moveCaretEnd() {
        this.getEditor().moveCaretEnd();
        this.requestFocus();
    }

    /**
     * 选中区间
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    public void selectRange(int start, int end) {
        this.getEditor().selectRange(start, end);
    }

    /**
     * 删除内容
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    public void deleteText(int start, int end) {
        this.getEditor().deleteText(start, end);
    }

    @Override
    public void requestFocus() {
        FXUtil.runWait(super::requestFocus);
    }

    @Override
    public void setContent(JComponent content) {
        super.setContent(content);
        SwingUtil.applyTheme(this.scrollPane);
    }
}
