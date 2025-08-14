package cn.oyzh.fx.editor.tem4javafx;

import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import jfx.incubator.scene.control.richtext.CodeArea;
import tm4java.grammar.IGrammarSource;
import tm4java.internal.utils.Resources;
import tm4java.theme.IThemeSource;
import tm4javafx.richtext.RichTextAreaModel;
import tm4javafx.richtext.StatelessSyntaxDecorator;
import tm4javafx.richtext.StyleHelper;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.TextFlowModel;

import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.Color;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 编辑器
 *
 * @author oyzh
 * @since 2025/07/30
 */
public class Editor extends CodeArea implements NodeAdapter, FlexAdapter, FontAdapter, ThemeAdapter {

    private final StyleProvider styleProvider = new StyleProvider();

    private final TextFlowModel textFlowModel = new TextFlowModel();

    private final RichTextAreaModel richTextAreaModel = new RichTextAreaModel();

    {

        textFlowModel.setStyleProvider(styleProvider);
        richTextAreaModel.setStyleProvider(styleProvider);
        // 格式变化事件
        this.formatTypeProperty().addListener((observableValue, formatType, t1) -> {
            this.initThemes();
            this.initSyntaxes();
        });
        // 提示词变化事件
        this.promptsProperty().addListener((observableValue, formatType, t1) -> {
            this.initPromptsStyle();
        });
        // 高亮变化事件
        this.highlightTextProperty().addListener((observableValue, formatType, t1) -> {
            this.initHighlightStyle();
        });
        // 文本变更事件
        this.addTextChangeListener((observableValue, s, t1) -> {
        });

        this.setSyntaxDecorator(new StatelessSyntaxDecorator(styleProvider));
        this.setLineNumbersEnabled(true);
        this.setHighlightCurrentParagraph(true);
    }

    protected void initSyntaxes() {
        try {
            EditorFormatType formatType = this.getFormatType();
            String name = formatType.toString().toLowerCase();
            String path = "/tm4javafx/grammars/" + name + ".tmLanguage.json";
            String url = ResourceUtil.getPath(path);
            this.styleProvider.setGrammar(IGrammarSource.fromFile(Path.of(url)));
            if (this.getSyntaxDecorator() instanceof StatelessSyntaxDecorator d) {
                d.refresh(this.getModel());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void initThemes() {
        try {
           this.changeTheme(ThemeManager.currentTheme());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 文本属性
     */
    private StringProperty textProperty;

    public StringProperty textProperty() {
        if (this.textProperty == null) {
            this.textProperty = new SimpleStringProperty(this.getText());
        }
        return this.textProperty;
    }

    /**
     * 添加文本监听器
     *
     * @param listener 监听器
     */
    public void addTextChangeListener(ChangeListener<String> listener) {
        synchronized (this) {
            this.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!this.ignoreChange) {
                    listener.changed(observable, oldValue, newValue);
                }
            });
        }
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

    public void setFormatType(EditorFormatType formatType) {
        this.formatTypeProperty().set(formatType);
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
            this.setDisable(true);
            this.setFormatType(formatType);
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
        } finally {
            this.setDisable(false);
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


    /**
     * 初始化高亮样式
     */
    protected void initHighlightStyle() {
    }

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
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        try {
            String str = this.getText();
            if (str == null || StringUtil.equalsAny(str, "\n", "\r", "\r\n")) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
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

    /**
     * 获取文本长度
     *
     * @return 文本长度
     */
    public int getLength() {
        return super.getText().length();
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
     * 遗忘历史
     */
    public void forgetHistory() {
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
            } catch (Exception ignored) {

            }
        }
    }

    /**
     * 追加行
     *
     * @param content 内容
     */
    public void appendLine(String content) {
        this.appendLine(content, true);
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
            String text = this.getText();
            ;
            if (text != null && !text.isEmpty() && !text.endsWith("\n") && !content.startsWith("\n")) {
                content = System.lineSeparator() + content;
            }
            if (endLine && !content.endsWith(System.lineSeparator())) {
                content = content + "\n";
            }
            this.appendText(content);
        }
    }


    /**
     * 获取光标边界
     *
     * @return 光标边界
     */
    public Optional<Bounds> getCaretBounds() {
        return Optional.empty();
    }

    /**
     * 选中选区
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    public void selectRange(int start, int end) {
    }

    /**
     * 设置光标位置
     *
     * @param caretPosition 光标位置
     */
    public void positionCaret(int caretPosition) {
    }

    /**
     * 移动光标到末尾
     */
    public void moveCaretEnd() {
        int len = this.getLength();
    }

    /**
     * 删除内容
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    public void deleteText(int start, int end) {
        try {
        } catch (Exception ignored) {

        }
    }

    public void setLineNumPolicy(EditorLineNumPolicy editorLineNumPolicy) {
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        ThemeAdapter.super.changeTheme(style);
        String path = "/tm4javafx/themes/vitesse-light.json";
        String url = ResourceUtil.getPath(path);
        this.styleProvider.setTheme(IThemeSource.fromFile(Path.of(url)));
        StyleHelper.applyThemeSettings(this, styleProvider.getThemeSettings());
    }
}
