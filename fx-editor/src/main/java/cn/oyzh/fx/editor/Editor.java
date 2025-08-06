package cn.oyzh.fx.editor;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.plus.swing.SwingUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        this.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                textProperty.setValue(getText());
            }
        });
        // 可编辑变更事件
        this.addPropertyChangeListener("editable", e -> {
            this.editableProperty.setValue((Boolean) e.getNewValue());
        });
    }

    private final BooleanProperty undoableProperty = new SimpleBooleanProperty();

    private final BooleanProperty redoableProperty = new SimpleBooleanProperty();

    private final BooleanProperty editableProperty = new SimpleBooleanProperty();

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
     * 数据类型
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

    private final StringProperty textProperty = new SimpleStringProperty();

    public void addTextChangeListener(ChangeListener<String> listener) {
        synchronized (this) {
            this.textProperty.addListener((observable, oldValue, newValue) -> {
                if (!this.ignoreChange) {
                    listener.changed(observable, oldValue, newValue);
                }
            });
        }
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
            switch (formatType) {
                case CSS -> this.showCssData(rawData);
                case RAW -> this.showRawData(rawData);
                case XML -> this.showXmlData(rawData);
                case JSON -> this.showJsonData(rawData);
                case HTML -> this.showHtmlData(rawData);
                case YAML -> this.showYamlData(rawData);
                case PROPERTIES -> this.showPropertiesData(rawData);
                default -> {
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
                }
            }
        } finally {
            this.ignoreChange = false;
            this.setEnabled(true);
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

    /**
     * 显示json数据
     */
    public void showJsonData(Object rawData) {
        String jsonData = TextUtil.getJsonData(rawData);
        this.setText(jsonData);
        this.setFormatType(EditorFormatType.JSON);
    }

    /**
     * 显示xml数据
     */
    public void showXmlData(Object rawData) {
        String xmlData = TextUtil.getXmlData(rawData);
        this.setText(xmlData);
        this.setFormatType(EditorFormatType.XML);
    }

    /**
     * 显示html数据
     */
    public void showHtmlData(Object rawData) {
        String htmlData = TextUtil.getHtmlData(rawData);
        this.setText(htmlData);
        this.setFormatType(EditorFormatType.HTML);
    }

    /**
     * 显示yaml数据
     */
    public void showYamlData(Object rawData) {
        String yamlData = TextUtil.getYamlData(rawData);
        this.setText(yamlData);
        this.setFormatType(EditorFormatType.YAML);
    }

    /**
     * 显示css数据
     */
    public void showCssData(Object rawData) {
        String yamlData = TextUtil.getCssData(rawData);
        this.setText(yamlData);
        this.setFormatType(EditorFormatType.CSS);
    }

    /**
     * 显示properties数据
     */
    public void showPropertiesData(Object rawData) {
        String yamlData = TextUtil.getPropertiesData(rawData);
        this.setText(yamlData);
        this.setFormatType(EditorFormatType.PROPERTIES);
    }

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
        SwingUtil.runWait(() -> super.setSyntaxEditingStyle(styleKey));
    }

    // public void invalidSyntaxEditingStyle() {
    //     SwingUtil.runWait(() -> super.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE));
    // }

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
        SwingUtil.runWait(() -> {
            try {
                Object id = this.getHighlighter().addHighlight(start, end, painter);
                reference.set(id);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
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
        SwingUtil.runWait(() -> {
            try {
                for (EditorHighlight highlight : highlights) {
                    Object id = this.getHighlighter().addHighlight(highlight.getStart(), highlight.getEnd(), painter);
                    ids.add(id);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return ids;
    }

    /**
     * 移除高亮
     *
     * @param id id
     */
    public void removeHighlight(Object id) {
        SwingUtil.runWait(() -> {
            try {
                this.getHighlighter().removeHighlight(id);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * 移除高亮
     *
     * @param ids id列表
     */
    public void removeHighlights(List<Object> ids) {
        SwingUtil.runWait(() -> {
            try {
                for (Object id : ids) {
                    this.getHighlighter().removeHighlight(id);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * 高亮样式
     */
    protected static final Highlighter.HighlightPainter HIGHLIGHT_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(
            new Color(248, 201, 171)
    );

    /**
     * 高亮id列表
     */
    private final List<Object> highlightIds = new CopyOnWriteArrayList<>();

    /**
     * 初始化高亮样式
     */
    protected void initHighlightStyle() {
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
        // 高亮正则模式
        Pattern highlightPattern = Pattern.compile(highlightText, Pattern.CASE_INSENSITIVE);
        // 生成高亮
        ThreadUtil.start(() -> {
            String text = this.getText();
            List<EditorHighlight> highlights = new ArrayList<>();
            Matcher matcher = highlightPattern.matcher(text);
            while (matcher.find()) {
                highlights.add(new EditorHighlight(matcher.start(), matcher.end()));
            }
            List<Object> ids = this.addHighlights(highlights, HIGHLIGHT_PAINTER);
            if (CollectionUtil.isNotEmpty(ids)) {
                synchronized (this.highlightIds) {
                    this.highlightIds.addAll(ids);
                }
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
     * 提示词高亮id列表
     */
    private final List<Object> promptIds = new CopyOnWriteArrayList<>();

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
     * 初始化提示词样式
     */
    protected void initPromptsStyle() {
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
        // 提示词正则模式
        Pattern promptsPattern = Pattern.compile("\\b(" + String.join("|", prompts) + ")\\b", Pattern.CASE_INSENSITIVE);
        // 生成高亮
        ThreadUtil.start(() -> {
            String text = this.getText();
            List<EditorHighlight> highlights = new ArrayList<>();
            Matcher matcher = promptsPattern.matcher(text);
            while (matcher.find()) {
                highlights.add(new EditorHighlight(matcher.start(), matcher.end()));
            }
            List<Object> ids = this.addHighlights(highlights, PROMPTS_PAINTER);
            synchronized (this.promptIds) {
                this.promptIds.addAll(ids);
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

    public String getTextTrim() {
        String text = this.getText();
        if (text == null) {
            return null;
        }
        return text.trim();
    }

    // @Override
    // public void cut() {
    //     SwingUtil.runWait(super::cut);
    // }
    //
    // @Override
    // public void copy() {
    //     SwingUtil.runWait(super::copy);
    // }
    //
    // @Override
    // public void paste() {
    //     SwingUtil.runWait(super::paste);
    // }

    public void undo() {
        super.undoLastAction();
        // SwingUtil.runWait(super::undoLastAction);
    }

    public void redo() {
        super.redoLastAction();
        // SwingUtil.runWait(super::redoLastAction);
    }

    public void forgetHistory() {
        super.discardAllEdits();
        // SwingUtil.runWait(this::discardAllEdits);
    }

    public void replaceText(int start, int end, String content) {
        if (content != null) {
            try {
                // SwingUtil.runWait(() -> super.replaceRange(content, start, end));
                super.replaceRange(content, start, end);
            } catch (Exception ignored) {

            }
        }
    }

    public void appendText(String content) {
        if (content != null) {
            super.append(content);
        }
    }

    public void appendLine(String content) {
        if (content != null) {
            String text = this.getText();
            if (text != null && !text.isEmpty() && !text.endsWith("\n") && !content.startsWith("\n")) {
                content = System.lineSeparator() + content;
            }
            if (!content.endsWith(System.lineSeparator())) {
                content = content + "\n";
            }
            this.append(content);
        }
    }

    @Override
    public void setFont(Font font) {
        try {
            // super.setFont(font);
            SwingUtil.runWait(() -> super.setFont(font));
            // SwingUtil.runLater(() -> super.setFont(font));
        } catch (Exception ignored) {

        }
    }

}
