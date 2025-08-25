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
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import com.sun.javafx.scene.NodeHelper;
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
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.SelectionSegment;
import jfx.incubator.scene.control.richtext.TextPos;
import tm4java.grammar.IGrammarSource;
import tm4java.theme.IThemeSource;
import tm4javafx.richtext.RichTextAreaModel;
import tm4javafx.richtext.StatelessSyntaxDecorator;
import tm4javafx.richtext.StyleHelper;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.TextFlowModel;

import java.nio.file.Path;
import java.util.ArrayList;
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
public class Editor extends CodeArea implements ContextMenuAdapter, MenuItemAdapter, NodeAdapter, FlexAdapter, FontAdapter, ThemeAdapter, TipAdapter, NodeGroup {

    /**
     * 默认提示词颜色
     */
    public static final Color DEFAULT_PROMPTS_COLOR = Color.rgb(125, 190, 93);

    /**
     * 默认高亮颜色
     */
    public static final Color DEFAULT_HIGHLIGHT_COLOR = Color.rgb(255, 102, 0);

    /**
     * 默认光标行颜色
     */
    public static final Color DEFAULT_CARET_LINE_COLOR = Color.rgb(255, 255, 205);

    /**
     * 默认光标行颜色-暗色模式
     */
    public static final Color DEFAULT_CARET_LINE_DARK_COLOR = Color.rgb(38, 40, 46);

    /**
     * 默认选区颜色
     */
    public static final Color DEFAULT_SELECTION_COLOR = Color.rgb(166, 210, 255);

    /**
     * 默认选区颜色-暗色模式
     */
    public static final Color DEFAULT_SELECTION_DARK_COLOR = Color.rgb(33, 66, 131);

    /**
     * 样式提供者
     */
    private final StyleProvider styleProvider = new StyleProvider();

    /**
     * 流式文本model
     */
    private final TextFlowModel textFlowModel = new TextFlowModel();

    /**
     * 富文本域model
     */
    private final RichTextAreaModel richTextAreaModel = new RichTextAreaModel();

    /**
     * 编辑器语法装饰器
     */
    private final EditorSyntaxDecorator syntaxDecorator = new EditorSyntaxDecorator();

    {
        NodeManager.init(this);
    }

    /**
     * 初始化编辑器
     */
    private void initEditor() {
        // 默认自动换行
        this.setWrapText(true);
        // 默认显示行号
        this.setLineNumbersEnabled(true);
        // 默认高亮当前行
        this.setHighlightCurrentParagraph(true);
        // 内容内边距
        this.setContentPadding(new Insets(5));
        // 行号装饰器
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
            if (!this.ignoreChange) {
                this.initTextStyle();
            }
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
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
        // 初始化样式
        this.applyTheme();
        // this.initTextStyle();
    }

    /**
     * 语法名称
     */
    private String syntaxesName;

    /**
     * 初始化语法
     */
    protected void initSyntaxes() {
        try {
            EditorFormatType formatType = this.getFormatType();
            if (formatType != EditorFormatType.RAW) {
                String syntaxesName = formatType.getFullSyntaxesName();
                // 如果未发生变化，则跳过
                if (StringUtil.equals(this.syntaxesName, syntaxesName)) {
                    return;
                }
                this.syntaxesName = syntaxesName;
                String path = "/tm4javafx/grammars/" + syntaxesName;
                String url = ResourceUtil.getPath(path);
                this.styleProvider.setGrammar(IGrammarSource.fromFile(Path.of(url)));
            }
            if (this.getSyntaxDecorator() instanceof StatelessSyntaxDecorator d) {
                d.refresh(this.getModel());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化文本样式
     */
    private void initTextStyle() {
        this.initSyntaxes();
        this.setText(this.getText());
    }

    /**
     * 文本属性
     */
    private StringProperty textProperty;

    public StringProperty textProperty() {
        if (this.textProperty == null) {
            this.textProperty = new SimpleStringProperty(this.getText());
            super.getModel().addListener(e -> {
                if (e.isEdit()) {
                    this.textProperty.setValue(this.getText());
                }
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
            String data = null;
            if (rawData instanceof CharSequence sequence) {
                data = sequence.toString();
            } else if (rawData instanceof byte[] array) {
                data = Arrays.toString(array);
            } else if (rawData != null) {
                data = rawData.toString();
            }
            // 设置格式
            this.setFormatType(formatType);
            if (data != null) {
                // 初始化语法格式
                this.initSyntaxes();
                // 设置内容
                this.setText(data);
            } else {
                this.clear();
            }
            // 忘记历史
            this.forgetHistory();
        } finally {
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
        } else if (detectType == 9) {
            formatType = EditorFormatType.YAML;
        } else {
            formatType = EditorFormatType.RAW;
        }
        this.showData(rawData, formatType);
        return formatType;
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
            if (StringUtil.isEmpty(str) || StringUtil.equalsAny(str, "\n", "\r", "\r\n")) {
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
        FXUtil.runWait(() -> {
            super.moveDocumentStart();
            super.requestFocus();
        });
    }

    /**
     * 移动光标到末尾
     */
    public void moveCaretEnd() {
        FXUtil.runWait(() -> {
            super.moveDocumentEnd();
            super.requestFocus();
        });
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
     * @param color 光标颜色
     */
    public void setCaretColor(Color color) {
        EditorSkin skin = (EditorSkin) this.getSkin();
        if (skin != null) {
            skin.setCaretColor(color);
        }
    }

    /**
     * 获取光标颜色
     *
     * @return 光标颜色
     */
    public Color getCaretColor() {
        EditorSkin skin = (EditorSkin) this.getSkin();
        return skin == null ? null : skin.getCaretColor();
    }

    /**
     * 设置光标行颜色
     *
     * @param color 光标行颜色
     */
    public void setCaretLineColor(Color color) {
        EditorSkin skin = (EditorSkin) this.getSkin();
        if (skin != null) {
            skin.setCaretLineColor(color);
        }
    }

    /**
     * 获取光标行颜色
     *
     * @return 光标行颜色
     */
    public Color getCaretLineColor() {
        EditorSkin skin = (EditorSkin) this.getSkin();
        return skin == null ? null : skin.getCaretLineColor();
    }

    /**
     * 获取默认光标行颜色
     *
     * @return 默认光标行颜色
     */
    protected Color defaultCaretLineColor() {
        if (ThemeManager.isDarkMode()) {
            return DEFAULT_CARET_LINE_DARK_COLOR;
        }
        return DEFAULT_CARET_LINE_COLOR;
    }

    /**
     * 设置选区颜色
     *
     * @param color 选区颜色
     */
    public void setSelectionColor(Color color) {
        EditorSkin skin = (EditorSkin) this.getSkin();
        if (skin != null) {
            skin.setSelectionColor(color);
        }
    }

    /**
     * 获取选区颜色
     *
     * @return 选区颜色
     */
    public Color getSelectionColor() {
        EditorSkin skin = (EditorSkin) this.getSkin();
        return skin == null ? null : skin.getSelectionColor();
    }

    /**
     * 获取默认选区颜色
     *
     * @return 默认选区颜色
     */
    protected Color defaultSelectionColor() {
        if (ThemeManager.isDarkMode()) {
            return DEFAULT_SELECTION_DARK_COLOR;
        }
        return DEFAULT_SELECTION_COLOR;
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        try {
            String path;
            if (style.isDarkMode()) {
                if (style == Themes.DRACULA) {
                    path = "/tm4javafx/themes/dracula.json";
                } else if (style == Themes.NORD_DARK) {
                    path = "/tm4javafx/themes/github-dark.json";
                } else if (style == Themes.PRIMER_DARK) {
                    path = "/tm4javafx/themes/min-dark.json";
                } else if (style == Themes.CUPERTINO_DARK) {
                    path = "/tm4javafx/themes/dark-plus.json";
                } else {
                    path = "/tm4javafx/themes/vitesse-dark.json";
                }
            } else {
                if (style == Themes.NORD_LIGHT) {
                    path = "/tm4javafx/themes/github-light.json";
                } else if (style == Themes.PRIMER_LIGHT) {
                    path = "/tm4javafx/themes/min-light.json";
                } else if (style == Themes.CUPERTINO_LIGHT) {
                    path = "/tm4javafx/themes/light-plus.json";
                } else {
                    path = "/tm4javafx/themes/vitesse-light.json";
                }
            }
            // 获取资源
            String url = ResourceUtil.getPath(path);
            // 设置主题
            this.styleProvider.setTheme(IThemeSource.fromFile(Path.of(url)));
            // 应用颜色
            StyleHelper.applyThemeSettings(this, this.styleProvider.getThemeSettings());
            // TODO: 修复主题色可能不生效问题
            NodeHelper.processCSS(this);
            // 设置光标行颜色
            this.setCaretLineColor(this.defaultCaretLineColor());
            // 设置选区颜色
            this.setSelectionColor(this.defaultSelectionColor());
            // 设置光标颜色
            this.setCaretColor(ThemeManager.currentAccentColor());
            // 初始化文字样式
            this.initTextStyle();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    /**
     * 隐藏行号
     */
    public void hideLineNum() {
        this.setLineNumbersEnabled(false);
    }

    /**
     * 显示行号
     */
    public void showLineNum() {
        this.setLineNumbersEnabled(true);
    }

    /**
     * 设置提示文本
     * 不支持此属性，已废弃
     *
     * @param promptText 提示文本
     */
    @Deprecated
    public void setPromptText(String promptText) {
    }

    /**
     * 获取提示文本
     * 不支持此属性，已废弃
     *
     * @return 提示文本
     */
    @Deprecated
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

    /**
     * 滚动到顶部
     */
    public void scrollToTop() {
        this.moveCaretStart();
    }

    /**
     * 滚动到末尾
     */
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

    /**
     * 是否选中了文本
     *
     * @return 结果
     */
    public boolean isSelectedText() {
        SelectionSegment segment = this.getSelection();
        if (segment == null) {
            return false;
        }
        TextPos min = segment.getMin();
        TextPos max = segment.getMax();
        return !min.equals(max);
    }

    /**
     * 格式化
     */
    public void formatting() {
        String text = this.getText();
        String text1 = EditorFormatter.formatText(this.getFormatType(), text);
        if (StringUtil.notEquals(text, text1)) {
            this.setText(text1);
        }
    }

    /**
     * 自动换行
     */
    public void wordWrap() {
        this.setWrapText(!this.isWrapText());
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        // 是否选中内容
        boolean selectedText = this.isSelectedText();

        // 撤销
        MenuItem undo = MenuItemManager.getMenuItem(I18nHelper.undo(), this::undo);
        undo.setDisable(!this.isUndoable());
        items.add(undo);

        // 重做
        MenuItem redo = MenuItemManager.getMenuItem(I18nHelper.redo(), this::redo);
        redo.setDisable(!this.isUndoable());
        items.add(redo);

        items.add(MenuItemManager.getSeparatorMenuItem());

        // 剪切
        MenuItem cut = MenuItemManager.getMenuItem(I18nHelper.cut(), this::cut);
        cut.setDisable(!selectedText || !this.isEditable() || this.isDisable());
        items.add(cut);

        // 复制
        MenuItem copy = MenuItemManager.getMenuItem(I18nHelper.copy(), this::copy);
        copy.setDisable(!selectedText || this.isDisable());
        items.add(copy);

        // 粘贴
        MenuItem paste = MenuItemManager.getMenuItem(I18nHelper.paste(), this::paste);
        paste.setDisable(!this.isEditable() || this.isDisable() || !ClipboardUtil.hasString());
        items.add(paste);

        items.add(MenuItemManager.getSeparatorMenuItem());

        // 全选
        MenuItem selectAll = MenuItemManager.getMenuItem(I18nHelper.selectAll(), this::selectAll);
        items.add(selectAll);

        // 自动换行
        MenuItem wordWrap = MenuItemManager.getMenuItem(I18nHelper.wordWrap(), this::wordWrap);
        items.add(wordWrap);

        // 移动到文档头
        MenuItem moveToTheBeginningOfTheDocument = MenuItemManager.getMenuItem(I18nHelper.moveToTheBeginningOfTheDocument(), this::moveCaretStart);
        items.add(moveToTheBeginningOfTheDocument);

        // 移动到文档尾
        MenuItem moveToTheEndOfTheDocument = MenuItemManager.getMenuItem(I18nHelper.moveToTheEndOfTheDocument(), this::moveCaretEnd);
        items.add(moveToTheEndOfTheDocument);

        // 格式化文档
        MenuItem formattingDocument = MenuItemManager.getMenuItem(I18nHelper.formattingDocument(), this::formatting);
        items.add(formattingDocument);

        return items;
    }
}
