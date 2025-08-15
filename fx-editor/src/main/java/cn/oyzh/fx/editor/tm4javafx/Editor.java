package cn.oyzh.fx.editor.tm4javafx;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.editor.EditorLineNumPolicy;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.ControlUtil;
import com.sun.jfx.incubator.scene.control.richtext.CaretInfo;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import tm4java.grammar.IGrammarSource;
import tm4java.theme.IThemeSource;
import tm4javafx.richtext.RichTextAreaModel;
import tm4javafx.richtext.StatelessSyntaxDecorator;
import tm4javafx.richtext.StyleHelper;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.TextFlowModel;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 编辑器
 *
 * @author oyzh
 * @since 2025/07/30
 */
public class Editor extends CodeArea implements NodeAdapter, FlexAdapter, FontAdapter, ThemeAdapter, TipAdapter {

    private final StyleProvider styleProvider = new StyleProvider();

    private final TextFlowModel textFlowModel = new TextFlowModel();

    private final RichTextAreaModel richTextAreaModel = new RichTextAreaModel();

    private final EditorSyntaxDecorator syntaxDecorator = new EditorSyntaxDecorator();

    {
        NodeManager.init(this);
    }

    /**
     * 初始化编辑器
     */
    private void initEditor() {
        // 默认显示行号
        this.setLineNumbersEnabled(true);
        // 内容内边距
        this.setContentPadding(new Insets(5));
        this.setLeftDecorator(new EditorLineNumberDecorator());
        // 边框
        Color color = ThemeManager.currentForegroundColor();
        CornerRadii radii = new CornerRadii(3);
        BorderStroke stroke = new BorderStroke(color, BorderStrokeStyle.SOLID, radii, ControlUtil.BW_HALF);
        Border border = new Border(stroke);
        this.setBorder(border);
        // 初始化组件
        this.textFlowModel.setStyleProvider(this.styleProvider);
        this.syntaxDecorator.setStyleProvider(this.styleProvider);
        this.richTextAreaModel.setStyleProvider(this.styleProvider);
        // 语法装饰
        this.setSyntaxDecorator(this.syntaxDecorator);

        // 格式变化事件
        this.formatTypeProperty().addListener((observableValue, formatType, t1) -> {
            this.syntaxDecorator.setFormatType(t1);
            this.initTextStyle();
        });
        // 提示词变化事件
        this.promptsProperty().addListener((observableValue, formatType, t1) -> {
            this.syntaxDecorator.setPrompts(t1);
            this.initTextStyle();
        });
        // 高亮变化事件
        this.highlightTextProperty().addListener((observableValue, formatType, t1) -> {
            this.syntaxDecorator.setHighlight(t1);
            this.initTextStyle();
        });
        // 行号策略变化事件
        this.lineNumPolicyProperty().addListener((observableValue, editorLineNumPolicy, t1) -> {
            if (t1 == EditorLineNumPolicy.NONE) {
                this.hideLineNum();
            } else if (t1 == EditorLineNumPolicy.ALWAYS) {
                this.showLineNum();
            }
        });
        // 初始化样式
        this.initTextStyle();
    }

    /**
     * 初始化主题
     */
    protected void initThemes() {
        try {
            this.changeTheme(ThemeManager.currentTheme());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化语法
     */
    protected void initSyntaxes() {
        try {
            EditorFormatType formatType = this.getFormatType();
            if (formatType != EditorFormatType.RAW) {
                String name = formatType.toString().toLowerCase();
                String path = "/tm4javafx/grammars/" + name + ".tmLanguage.json";
                String url = ResourceUtil.getPath(path);
                this.styleProvider.setGrammar(IGrammarSource.fromFile(Path.of(url)));
            }
            if (this.getSyntaxDecorator() instanceof StatelessSyntaxDecorator d) {
                d.refresh(this.getModel());
            }
            this.setText(this.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化文本样式
     */
    private void initTextStyle() {
        this.initThemes();
        this.initSyntaxes();
    }

    /**
     * 文本属性
     */
    private StringProperty textProperty;

    public StringProperty textProperty() {
        if (this.textProperty == null) {
            this.textProperty = new SimpleStringProperty(this.getText());
            super.getModel().addListener(ch -> {
                this.textProperty.setValue(this.getText());
            });
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
     * 设置样式
     *
     * @param start 开始位置
     * @param end   结束位置
     * @param color 颜色
     */
    public void setStyle(int start, int end, Color color) {
        System.out.println(start + "=" + end);
        EditorTextPos pos = this.getPosByIndex(start, end);
        StyleAttributeMap attributeMap = StyleAttributeMap.of(StyleAttributeMap.TEXT_COLOR, color);
        super.setStyle(pos.getStart(), pos.getEnd(), attributeMap);
    }

    public void setStyles(List<EditorStyle> styles) {
        for (EditorStyle style : styles) {
            this.setStyle(style.start(), style.end(), style.color());
        }
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
        String text = super.getText();
        if (text == null) {
            return 0;
        }
        return text.length();
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
        this.clearUndoRedo();
    }

    /**
     * 获取位置
     *
     * @param start 开始位置
     * @param end   结束位置
     * @return 位置
     */
    protected EditorTextPos getPosByIndex(int start, int end) {
        int length = 0;
        int endIndex = -1;
        int startIndex = -1;
        int pCount = super.getParagraphCount();
        for (int i = 0; i < pCount; i++) {
            int len = super.getModel().getParagraphLength(i);
            length += len + 1;
            if (startIndex == -1 && length >= start) {
                startIndex = i;
            }
            if (length >= end) {
                endIndex = i;
                break;
            }
        }
        TextPos endPos = TextPos.ofLeading(endIndex, end);
        TextPos startPos = TextPos.ofLeading(startIndex, start);
        return new EditorTextPos(startPos, endPos);
    }

    /**
     * 根据文本位置获取位置
     *
     * @param pos 文本位置
     * @return 位置
     */
    protected int getOffsetByPos(TextPos pos) {
        int length = 0;
        for (int i = 0; i < pos.index(); i++) {
            int len = super.getModel().getParagraphLength(i);
            length += len + 1;
        }
        return length + pos.offset();
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
                EditorTextPos pos = this.getPosByIndex(start, end);
                super.replaceText(pos.getStart(), pos.getEnd(), content, true);
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
            String text = this.getText();
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
        EditorSkin skin = (EditorSkin) this.getSkin();
        if (skin == null) {
            return Optional.empty();
        }
        VFlow flow = skin.getVFlow();
        if (flow == null) {
            return Optional.empty();
        }
        CaretInfo caretInfo = flow.getCaretInfo();
        if (caretInfo == null) {
            return Optional.empty();
        }
        TextPos pos = this.getCaretPosition();
        int lLen = (pos.index() + "").length();
        Point2D point2D = this.localToScreen(0, 0);
        double fSize = FontUtil.stringWidth("a", this.getFont());
        // 获取内边距左
        Insets insets = this.getContentPadding();
        double insetsLeft = insets == null ? 0 : insets.getLeft();
        // 获取边框宽
        Border border = this.getBorder();
        BorderStroke stroke = border == null ? null : CollectionUtil.getFirst(border.getStrokes());
        double borderW = stroke == null ? 0 : stroke.getWidths().getLeft();
        // 获取左侧宽 = 内边距左 + 边框左 + (内容长度 + 1) * 字符宽
        double leftW = insetsLeft + borderW + (lLen + 1) * fSize;
        double x = point2D.getX() + caretInfo.getMaxX() + leftW;
        double y = point2D.getY() + caretInfo.getMaxY();
        BoundingBox box = new BoundingBox(x, y, fSize, fSize);
        return Optional.of(box);
    }

    /**
     * 选中选区
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    public void selectRange(int start, int end) {
        EditorTextPos pos = this.getPosByIndex(start, end);
        super.select(pos.getStart(), pos.getEnd());
    }

    /**
     * 设置光标位置
     *
     * @param caretPosition 光标位置
     */
    public void positionCaret(int caretPosition) {
        this.selectRange(caretPosition, caretPosition);
        super.requestFocus();
    }

    /**
     * 移动光标到头部
     */
    public void moveCaretStart() {
        super.moveDocumentStart();
        super.requestFocus();
    }

    /**
     * 移动光标到末尾
     */
    public void moveCaretEnd() {
        super.moveDocumentEnd();
        super.requestFocus();
    }

    /**
     * 删除内容
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    public void deleteText(int start, int end) {
        try {
            this.replaceText(start, end, "");
        } catch (Exception ignored) {

        }
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

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    protected EditorSkin createDefaultSkin() {
        return new EditorSkin(this);
    }

    /**
     * 设置光标颜色
     *
     * @param color 颜色
     */
    public void setCartColor(Color color) {
        EditorSkin skin = (EditorSkin) this.getSkin();
        if (skin != null) {
            skin.setCartColor(color);
        }
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        ThemeAdapter.super.changeTheme(style);
        String path;
        if (style.isDarkMode()) {
            this.setCartColor(Color.WHITE);
            path = "/tm4javafx/themes/vitesse-dark.json";
        } else {
            this.setCartColor(Color.BLACK);
            path = "/tm4javafx/themes/vitesse-light.json";
        }
        String url = ResourceUtil.getPath(path);
        this.styleProvider.setTheme(IThemeSource.fromFile(Path.of(url)));
        StyleHelper.applyThemeSettings(this, styleProvider.getThemeSettings());
    }

    @Override
    public void initNode() {
        // 初始化编辑器
        this.initEditor();
        // 尝试初始化提示词
        this.setPrompts(this.getPrompts());
        // 尝试初始化高亮
        this.setHighlightText(this.getHighlightText());
    }

    public void hideLineNum() {
        this.setLineNumbersEnabled(false);
    }

    public void showLineNum() {
        this.setLineNumbersEnabled(true);
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
    public int caretPosition() {
        TextPos pos = super.getCaretPosition();
        if (pos == null) {
            return -1;
        }
        return this.getOffsetByPos(pos);
    }

    public void scrollToTop() {
        this.moveCaretStart();
    }

    public void scrollToEnd() {
        this.moveCaretEnd();
    }

    /**
     * 增加字号
     */
    public void fontSizeIncr() {
        this.setFontSize(this.getFontSize() + 1);
    }

    /**
     * 减少字号
     */
    public void fontSizeDecr() {
        this.setFontSize(this.getFontSize() - 1);
    }


}
