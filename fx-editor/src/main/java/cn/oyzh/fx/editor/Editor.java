package cn.oyzh.fx.editor;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.plus.swing.SwingUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编辑器
 *
 * @author oyzh
 * @since 2025/07/30
 */
public class Editor extends TextEditorPane {

    /**
     * 文本属性
     */
    private final StringProperty textProperty = new SimpleStringProperty();

    /**
     * 添加文本监听器
     *
     * @param listener 监听器
     */
    public void addTextChangeListener(ChangeListener<String> listener) {
        synchronized (this) {
            this.textProperty.addListener((observable, oldValue, newValue) -> {
                if (!this.ignoreChange) {
                    listener.changed(observable, oldValue, newValue);
                }
            });
        }
    }

    {
        // 格式变化事件
        this.formatTypeProperty().addListener((observableValue, formatType, t1) -> {
            this.initTextStyle();
        });
        // 提示词变化事件
        this.promptsProperty().addListener((observableValue, formatType, t1) -> {
            this.initPromptsStyle();
        });
        // 高亮变化事件
        this.highlightTextProperty().addListener((observableValue, formatType, t1) -> {
            this.initHighlightStyle();
        });
        // 重做、撤销监听器
        this.addUndoableEditListener(e -> {
            this.redoableProperty.set(e.getEdit().canRedo());
            this.undoableProperty.set(e.getEdit().canUndo());
        });
        // 文本变更事件
        this.addDocumentListener(new EditorDocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                textProperty.setValue(getText());
            }
        });
        // 光标位置变更事件
        this.addCaretListener(e -> {
            this.caretPositionProperty.set(e.getDot());
        });
        // 可编辑变更事件
        this.addPropertyChangeListener("editable", e -> {
            this.editableProperty.setValue((Boolean) e.getNewValue());
        });
        // 文本变更事件
        this.addTextChangeListener((observableValue, s, t1) -> {
            this.initTextStyle();
        });
    }

    /**
     * 光标位置属性
     */
    private final LongProperty caretPositionProperty = new SimpleLongProperty();

    /**
     * 可撤销属性
     */
    private final BooleanProperty undoableProperty = new SimpleBooleanProperty();

    /**
     * 可重做属性
     */
    private final BooleanProperty redoableProperty = new SimpleBooleanProperty();

    /**
     * 可编辑属性
     */
    private final BooleanProperty editableProperty = new SimpleBooleanProperty();

    public LongProperty caretPositionProperty() {
        return caretPositionProperty;
    }

    public BooleanProperty undoableProperty() {
        return undoableProperty;
    }

    public BooleanProperty redoableProperty() {
        return redoableProperty;
    }

    public BooleanProperty editableProperty() {
        return editableProperty;
    }

    /**
     * 是否忽略变化
     */
    private boolean ignoreChange;

    /**
     * 格式类型
     */
    private ObjectProperty<EditorFormatType> formatTypeProperty;

    public EditorFormatType getFormatType() {
        return this.formatTypeProperty == null ? EditorFormatType.RAW : this.formatTypeProperty.get();
    }

    public void setFormatType(EditorFormatType formatTypeProperty) {
        this.formatTypeProperty().set(formatTypeProperty);
    }

    public ObjectProperty<EditorFormatType> formatTypeProperty() {
        if (this.formatTypeProperty == null) {
            this.formatTypeProperty = new SimpleObjectProperty<>(EditorFormatType.RAW);
        }
        return formatTypeProperty;
    }

    /**
     * 显示数据
     *
     * @param rawData 显示数据
     */
    public void showData(Object rawData) {
        this.showData(rawData, this.getFormatType());
    }

    /**
     * 显示数据
     *
     * @param rawData    显示数据
     * @param formatType 格式类型
     */
    public void showData(Object rawData, EditorFormatType formatType) {
        try {
            this.ignoreChange = true;
            this.setEnabled(false);
            // switch (formatType) {
            //     case CSS -> this.showCssData(rawData);
            //     case RAW -> this.showRawData(rawData);
            //     case XML -> this.showXmlData(rawData);
            //     case JSON -> this.showJsonData(rawData);
            //     case HTML -> this.showHtmlData(rawData);
            //     case YAML -> this.showYamlData(rawData);
            //     case PROPERTIES -> this.showPropertiesData(rawData);
            //     default -> {
            String data = null;
            if (rawData instanceof CharSequence sequence) {
                data = sequence.toString();
            } else if (rawData instanceof byte[] array) {
                data = Arrays.toString(array);
            } else if (rawData != null) {
                data = rawData.toString();
            }
            if (data != null) {
                this.setText(data);
            } else {
                this.clear();
            }
            this.setFormatType(formatType);
            // }
            // }
        } finally {
            this.setEnabled(true);
            this.ignoreChange = false;
        }
    }

    /**
     * 显示检测后的数据
     *
     * @param rawData 显示数据
     * @return EditorFormatType
     */
    public EditorFormatType showDetectData(Object rawData) {
        // 检测类型
        byte detectType = TextUtil.detectType(rawData);
        EditorFormatType formatType;
        if (detectType == 1) {
            formatType = EditorFormatType.JSON;
        } else if (detectType == 3) {
            formatType = EditorFormatType.XML;
        } else if (detectType == 4) {
            formatType = EditorFormatType.HTML;
        } else if (detectType == 7) {
            formatType = EditorFormatType.CSS;
        } else if (detectType == 8) {
            formatType = EditorFormatType.PROPERTIES;
        } else if (detectType == 9) {
            formatType = EditorFormatType.YAML;
        } else {
            formatType = EditorFormatType.RAW;
        }
        this.showData(rawData, formatType);
        return formatType;
    }

    @Override
    public void setText(String text) {
        try {
            if (StringUtil.isEmpty(text)) {
                this.clear();
            } else {
                SwingUtil.runWait(() -> super.setText(text));
            }
        } catch (Throwable ignore) {

        }
    }

    // /**
    //  * 显示json数据
    //  */
    // public void showJsonData(Object rawData) {
    //     String data = TextUtil.getJsonData(rawData);
    //     this.setText(data);
    //     this.setFormatType(EditorFormatType.JSON);
    // }
    //
    // /**
    //  * 显示xml数据
    //  */
    // public void showXmlData(Object rawData) {
    //     String data = TextUtil.getXmlData(rawData);
    //     this.setText(data);
    //     this.setFormatType(EditorFormatType.XML);
    // }
    //
    // /**
    //  * 显示html数据
    //  */
    // public void showHtmlData(Object rawData) {
    //     String data = TextUtil.getHtmlData(rawData);
    //     this.setText(data);
    //     this.setFormatType(EditorFormatType.HTML);
    // }
    //
    // /**
    //  * 显示yaml数据
    //  */
    // public void showYamlData(Object rawData) {
    //     String data = TextUtil.getYamlData(rawData);
    //     this.setText(data);
    //     this.setFormatType(EditorFormatType.YAML);
    // }
    //
    // /**
    //  * 显示css数据
    //  */
    // public void showCssData(Object rawData) {
    //     String data = TextUtil.getCssData(rawData);
    //     this.setText(data);
    //     this.setFormatType(EditorFormatType.CSS);
    // }
    //
    // /**
    //  * 显示properties数据
    //  */
    // public void showPropertiesData(Object rawData) {
    //     String data = TextUtil.getPropertiesData(rawData);
    //     this.setText(data);
    //     this.setFormatType(EditorFormatType.PROPERTIES);
    // }

    /**
     * 显示原始数据
     */
    public void showRawData(Object rawData) {
        if (rawData instanceof CharSequence sequence) {
            this.setText(sequence.toString());
        } else if (rawData instanceof byte[] bytes) {
            this.setText(new String(bytes));
        } else {
            this.setText(rawData == null ? "" : rawData.toString());
        }
        this.setFormatType(EditorFormatType.RAW);
    }

    /**
     * 初始化样式
     */
    public void initTextStyle() {
        String editingStyle = this.getSyntaxEditingStyle();
        String syntax = EditorUtil.toSyntax(this.getFormatType());
        if (!StringUtil.equalsAnyIgnoreCase(editingStyle, syntax)) {
            this.setSyntaxEditingStyle(syntax);
        }
        this.initPromptsStyle();
        this.initHighlightStyle();
    }

    @Override
    public void setSyntaxEditingStyle(String styleKey) {
        SwingUtil.runLater(() -> super.setSyntaxEditingStyle(styleKey));
    }

    /**
     * 添加高亮
     *
     * @param start   开始位置
     * @param end     结束位置
     * @param painter 样式
     * @return id
     */
    public Object addHighlight(int start, int end, Highlighter.HighlightPainter painter) {
        AtomicReference<Object> reference = new AtomicReference<>();
        // SwingUtil.runWait(() -> {
        try {
            Object id = this.getHighlighter().addHighlight(start, end, painter);
            reference.set(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // });
        return reference.get();
    }

    /**
     * 添加高亮
     *
     * @param highlights 高亮列表
     * @param painter    样式
     * @return id列表
     */
    public List<Object> addHighlights(List<EditorHighlight> highlights, Highlighter.HighlightPainter painter) {
        List<Object> ids = new ArrayList<>();
        // SwingUtil.runWait(() -> {
        try {
            for (EditorHighlight highlight : highlights) {
                Object id = this.getHighlighter().addHighlight(highlight.getStart(), highlight.getEnd(), painter);
                ids.add(id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // });
        return ids;
    }

    /**
     * 移除高亮
     *
     * @param id id
     */
    public void removeHighlight(Object id) {
        // SwingUtil.runWait(() -> {
        try {
            this.getHighlighter().removeHighlight(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // });
    }

    /**
     * 移除高亮
     *
     * @param ids id列表
     */
    public void removeHighlights(List<Object> ids) {
        // SwingUtil.runWait(() -> {
        try {
            for (Object id : ids) {
                this.getHighlighter().removeHighlight(id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // });
    }

    /**
     * 高亮样式
     */
    protected static final Highlighter.HighlightPainter HIGHLIGHT_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(
            new Color(248, 201, 171)
    );

    /**
     * 提示词样式线程
     */
    private Thread highlightStyleThread;

    /**
     * 高亮id列表
     */
    private final List<Object> highlightIds = new CopyOnWriteArrayList<>();

    /**
     * 初始化高亮样式
     */
    protected void initHighlightStyle() {
        // 停止旧线程
        ThreadUtil.interrupt(this.highlightStyleThread);
        // 移除高亮
        if (!this.highlightIds.isEmpty()) {
            List<Object> ids;
            synchronized (this.highlightIds) {
                ids = new ArrayList<>(this.highlightIds);
                this.highlightIds.clear();
            }
            ThreadUtil.start(() -> this.removeHighlights(ids));
        }
        // 无高亮内容，返回
        String highlightText = this.getHighlightText();
        if (StringUtil.isEmpty(highlightText)) {
            return;
        }
        // 生成高亮
        Pattern highlightPattern = Pattern.compile(highlightText, Pattern.CASE_INSENSITIVE);
        this.highlightStyleThread = ThreadUtil.start(() -> {
            String text = this.getText();
            // 高亮正则模式
            Matcher matcher = highlightPattern.matcher(text);
            while (matcher.find() && !Thread.currentThread().isInterrupted()) {
                Object id = this.addHighlight(matcher.start(), matcher.end(), HIGHLIGHT_PAINTER);
                this.highlightIds.add(id);
            }
        });
    }

    /**
     * 提示词样式
     */
    protected static final Highlighter.HighlightPainter PROMPTS_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(
            new Color(125, 190, 93)
            // new Color(166, 38, 164)
    );

    /**
     * 提示词属性
     */
    private ObjectProperty<Set<String>> promptsProperty;

    public ObjectProperty<Set<String>> promptsProperty() {
        if (this.promptsProperty == null) {
            this.promptsProperty = new SimpleObjectProperty<>();
        }
        return this.promptsProperty;
    }

    /**
     * 设置提示词
     *
     * @param prompts 提示词列表
     */
    public void setPrompts(Set<String> prompts) {
        this.promptsProperty().set(prompts);
    }

    /**
     * 获取提示词
     *
     * @return 提示词列表
     */
    public Set<String> getPrompts() {
        return this.promptsProperty == null ? null : this.promptsProperty.get();
    }

    /**
     * 提示词样式线程
     */
    private Thread promptsStyleThread;

    /**
     * 提示词高亮id列表
     */
    private final List<Object> promptIds = new CopyOnWriteArrayList<>();

    /**
     * 初始化提示词样式
     */
    protected void initPromptsStyle() {
        // 停止旧线程
        ThreadUtil.interrupt(this.promptsStyleThread);
        // 移除提示词
        if (!this.promptIds.isEmpty()) {
            List<Object> ids;
            synchronized (this.promptIds) {
                ids = new ArrayList<>(this.promptIds);
                this.promptIds.clear();
            }
            ThreadUtil.start(() -> this.removeHighlights(ids));
        }
        // 无高亮内容，返回
        Set<String> prompts = this.getPrompts();
        if (CollectionUtil.isEmpty(prompts)) {
            return;
        }
        // 生成高亮
        Pattern promptsPattern = Pattern.compile("\\b(" + String.join("|", prompts) + ")\\b", Pattern.CASE_INSENSITIVE);
        this.promptsStyleThread = ThreadUtil.start(() -> {
            String text = this.getText();
            // 提示词正则模式
            Matcher matcher = promptsPattern.matcher(text);
            while (matcher.find() && !Thread.currentThread().isInterrupted()) {
                Object id = this.addHighlight(matcher.start(), matcher.end(), PROMPTS_PAINTER);
                this.promptIds.add(id);
            }
        });
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        AtomicBoolean result = new AtomicBoolean(false);
        SwingUtil.runWait(() -> {
            try {
                String str = this.getDocument().getText(0, 1);
                if (str == null || StringUtil.equalsAny(str, "\n", "\r", "\r\n")) {
                    result.set(true);
                }
            } catch (Exception ex) {
                result.set(true);
            }
        });
        return result.get();
    }

    /**
     * 高亮文本
     */
    private StringProperty highlightTextProperty;

    public String getHighlightText() {
        return this.highlightTextProperty == null ? null : this.highlightTextProperty.get();
    }

    /**
     * 设置高亮文本
     *
     * @param highlightText 高亮文本
     */
    public void setHighlightText(String highlightText) {
        this.highlightTextProperty().set(highlightText);
    }

    public StringProperty highlightTextProperty() {
        if (this.highlightTextProperty == null) {
            this.highlightTextProperty = new SimpleStringProperty();
        }
        return this.highlightTextProperty;
    }

    public int getLength() {
        AtomicInteger val = new AtomicInteger();
        SwingUtil.runWait(() -> {
            int i = this.getDocument().getLength();
            val.set(i);
        });
        return val.get();
    }

    public void clear() {
        SwingUtil.runWait(() -> {
            try {
                this.getDocument().remove(0, this.getLength());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void addDocumentListener(DocumentListener documentListener) {
        SwingUtil.runWait(() -> this.getDocument().addDocumentListener(documentListener));
    }

    public void addUndoableEditListener(UndoableEditListener undoableEditListener) {
        SwingUtil.runWait(() -> this.getDocument().addUndoableEditListener(undoableEditListener));
    }

    /**
     * 获取内容，去除前后空格
     *
     * @return 内容
     */
    public String getTextTrim() {
        String text = this.getText();
        if (text == null) {
            return null;
        }
        return text.trim();
    }

    /**
     * 撤销
     */
    public void undo() {
        super.undoLastAction();
    }

    /**
     * 重做
     */
    public void redo() {
        super.redoLastAction();
    }

    /**
     * 遗忘历史
     */
    public void forgetHistory() {
        super.discardAllEdits();
    }

    /**
     * 替换内容
     *
     * @param start   开始位置
     * @param end     结束位置
     * @param content 内容
     */
    public void replaceText(int start, int end, String content) {
        if (content != null) {
            try {
                super.replaceRange(content, start, end);
            } catch (Exception ignored) {

            }
        }
    }

    /**
     * 追加内容
     *
     * @param content 内容
     */
    public void appendText(String content) {
        if (content != null) {
            super.append(content);
        }
    }

    /**
     * 追加行
     *
     * @param content 内容
     */
    public void appendLine(String content) {
        this.appendLine(content, false);
    }

    /**
     * 追加行
     *
     * @param content 内容
     * @param endLine 尾部是否追加换行符
     */
    public void appendLine(String content, boolean endLine) {
        if (content != null) {
            int len = this.getLength();
            String text = null;
            try {
                this.getText(len - 1, len);
            } catch (Exception ex) {
                text = this.getText();
            }
            if (text != null && !text.isEmpty() && !text.endsWith("\n") && !content.startsWith("\n")) {
                content = System.lineSeparator() + content;
            }
            if (endLine && !content.endsWith(System.lineSeparator())) {
                content = content + "\n";
            }
            this.append(content);
        }
    }

    @Override
    public void setFont(Font font) {
        try {
            super.setFont(font);
            // SwingUtil.runTask(() -> super.setFont(font));
        } catch (Exception ignored) {

        }
    }

    /**
     * 获取光标边界
     *
     * @return 光标边界
     */
    public Optional<Bounds> getCaretBounds() {
        try {
            // 1. 获取光标在文档中的偏移量
            int caretOffset = this.getCaretPosition();
            // 2. 将文档偏移量转换为文本区域内的坐标（相对于文本区域的左上角）
            // 注意：viewToModel 和 modelToView 是 Swing 文本组件的核心坐标转换方法
            Rectangle caretRect = this.modelToView(caretOffset);
            // 3. 计算光标在屏幕上的绝对坐标
            //  - caretRect.x/y 是光标在文本区域内的相对坐标
            //  - 加上文本区域在屏幕上的绝对坐标，得到最终位置
            if (caretRect != null) {
                Point textAreaScreenPos = this.getLocationOnScreen();
                int screenX = textAreaScreenPos.x + caretRect.x;
                int screenY = textAreaScreenPos.y + caretRect.y;
                double screenScale = FXUtil.screenScale();
                double x = screenX / screenScale;
                double y = screenY / screenScale;
                BoundingBox bounds = new BoundingBox(x, y, x, y);
                return Optional.of(bounds);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * 选中选区
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    public void selectRange(int start, int end) {
        SwingUtil.runWait(() -> this.select(start, end));
    }

    @Override
    public Rectangle2D modelToView2D(int pos) {
        AtomicReference<Rectangle2D> reference = new AtomicReference<>();
        SwingUtil.runWait(() -> {
            try {
                reference.set(super.modelToView2D(pos));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return reference.get();
    }

    public void positionCaret(int caretPosition) {
        this.setCaretPosition(caretPosition);
    }

    public void deleteText(int start, int end) {
        try {
            this.getDocument().remove(start, end);
        } catch (Exception ignored) {

        }
    }
}
