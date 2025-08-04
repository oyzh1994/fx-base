package cn.oyzh.fx.editor;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.RegexHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.plus.controls.swing.FXSwingNode;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.util.SwingUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import cn.oyzh.fx.rich.richtextfx.control.BaseRichTextArea;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
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
        // 高亮变化事件
        this.highlightTextProperty().addListener((observableValue, formatType, t1) -> {
            this.initHighlightStyle();
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
        if (StringUtil.isNotBlank(this.getHighlightText())) {
            this.initHighlightStyle();
        }
    }

    @Override
    public void setSyntaxEditingStyle(String styleKey) {
        SwingUtil.runWait(() -> super.setSyntaxEditingStyle(styleKey));
    }

    public void invalidSyntaxEditingStyle() {
        SwingUtil.runWait(() -> super.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE));
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
     * 初始化高亮
     */
    protected void initHighlightStyle() {
        // 移除高亮
        ThreadUtil.start(() -> {
            List<Object> ids;
            synchronized (this.highlightIds) {
                ids = new ArrayList<>(this.highlightIds);
                this.highlightIds.clear();
            }
            this.removeHighlights(ids);
        });
        // 没有高亮内容，返回
        if (StringUtil.isBlank(this.getHighlightText())) {
            return;
        }
        // 生成高亮
        ThreadUtil.start(() -> {
            String text = this.getText();
            List<EditorHighlight> highlights = new ArrayList<>();
            Matcher matcher = this.highlightPattern().matcher(text);
            while (matcher.find()) {
                EditorHighlight highlight = new EditorHighlight();
                highlight.setStart(matcher.start());
                highlight.setEnd(matcher.end());
                highlights.add(highlight);
            }
            this.addHighlights(highlights);
        });
    }

    /**
     * 添加高亮
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    public void addHighlight(int start, int end) {
        SwingUtil.runLater(() -> {
            try {
                Object id = this.getHighlighter().addHighlight(start, end, HIGHLIGHT_PAINTER);
                this.highlightIds.add(id);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * 添加高亮
     *
     * @param highlights 高亮列表
     */
    public void addHighlights(List<EditorHighlight> highlights) {
        SwingUtil.runLater(() -> {
            try {
                for (EditorHighlight highlight : highlights) {
                    Object id = this.getHighlighter().addHighlight(highlight.getStart(), highlight.getEnd(), HIGHLIGHT_PAINTER);
                    this.highlightIds.add(id);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
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

    public boolean isEmpty() {
        String text = this.textProperty.get();
        return StringUtil.isEmpty(text);
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
        this.highlightTextProperty().setValue(highlightText);
    }

    public StringProperty highlightTextProperty() {
        if (this.highlightTextProperty == null) {
            this.highlightTextProperty = new SimpleStringProperty();
        }
        return this.highlightTextProperty;
    }

    /**
     * 高亮正则模式
     *
     * @return 高亮正则模式
     */
    protected Pattern highlightPattern() {
        return Pattern.compile(this.getHighlightText(), Pattern.CASE_INSENSITIVE);
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

    public void cut() {
        SwingUtil.runWait(super::cut);
    }

    public void copy() {
        SwingUtil.runWait(super::copy);
    }

    public void paste() {
        SwingUtil.runWait(super::paste);
    }

    public void addDocumentListener(DocumentListener documentListener) {
        SwingUtil.runWait(() -> this.getDocument().addDocumentListener(documentListener));
    }

    public String getTextTrim() {
        String text = this.getText();
        if (text == null) {
            return null;
        }
        return text.trim();
    }
}
