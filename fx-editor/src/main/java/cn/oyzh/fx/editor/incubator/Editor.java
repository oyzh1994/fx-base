package cn.oyzh.fx.editor.incubator;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.ObjectUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.plus.RemoveNodeable;
import cn.oyzh.fx.plus.adapter.ScrollBarAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import com.sun.javafx.scene.NodeHelper;
import com.sun.jfx.incubator.scene.control.richtext.CaretInfo;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.SelectionSegment;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;
import tm4java.grammar.IGrammarSource;
import tm4java.theme.IThemeSource;
import tm4javafx.richtext.RichTextAreaModel;
import tm4javafx.richtext.StatelessSyntaxDecorator;
import tm4javafx.richtext.StyleHelper;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.TextFlowModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编辑器
 *
 * @author oyzh
 * @since 2025-08-14
 */
public class Editor extends CodeArea implements RemoveNodeable, ScrollBarAdapter, ContextMenuAdapter, MenuItemAdapter, FlexAdapter, FontAdapter, ThemeAdapter, TipAdapter, NodeGroup, Destroyable {

    /**
     * 默认提示词颜色
     */
    public static final Color DEFAULT_PROMPTS_COLOR = Color.rgb(125, 190, 93);

    /**
     * 默认高亮颜色
     */
    public static final Color DEFAULT_HIGHLIGHT_COLOR = Color.rgb(216, 222, 231);
    //    public static final Color DEFAULT_HIGHLIGHT_COLOR = Color.valueOf("#D8DEE7");
    //    public static final Color DEFAULT_HIGHLIGHT_COLOR = Color.rgb(255, 102, 0);

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
     * 超链接常见顶级域名
     */
    private static final String HYPERLINK_TLDS = "com|cn|net|org|io|dev|app|cloud|ai|co|me|info|xyz|top|site|tech|online|store|edu|gov|mil|int|us|uk|de|jp|fr|ru|in|au|ca|hk|tw|sg|kr|eu|nl|se|ch|it|es|br|mx";

    /**
     * 默认超链接匹配模式。
     * <p>
     * 支持带协议的链接、www 开头、邮箱以及不带协议的常见域名。
     */
    private static final String HYPERLINK_URL_CHARS = "A-Za-z0-9\\-._~:/?#\\[\\]@!$&()*+,;=%";

    public static final Pattern DEFAULT_HYPERLINK_PATTERN = Pattern.compile("(?i)\\b("
            + "(?:(?:https?|ftps?|file|mailto|ssh|git|smb|ws|wss|telnet|redis|jdbc):(?://)?[" + HYPERLINK_URL_CHARS + "]+)"
            + "|(?:www\\.[" + HYPERLINK_URL_CHARS + "]+)"
            + "|(?:[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+)"
            + "|(?:(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+(?:" + HYPERLINK_TLDS + ")(?::\\d{1,5})?(?:/[" + HYPERLINK_URL_CHARS + "]*)?)"
            + ")");

    /**
     * 超链接尾部需要剔除的标点
     */
    private static final String HYPERLINK_TRAILING_CHARS = ".,;:!?)]}>\"'`*~-，。；：！？）】」》";

    /**
     * 参与超链接检测的段落下限长度，过长的行跳过检测以避免影响性能
     */
    private static final int HYPERLINK_MAX_PARAGRAPH_LENGTH = 200_000;

    /**
     * 成对符号映射 (开始 → 结束)
     */
    private static final Map<String, String> PAIR_MAP = Map.of(
            "{", "}",
            "(", ")",
            "[", "]",
            "\"", "\"",
            "'", "'",
            "`", "`",
            "<", ">"
    );

    /**
     * 样式提供者
     */
    private final StyleProvider styleProvider = new EditorStyleProvider();

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

    /**
     * 默认边距
     */
    private static final Insets DEFAULT_PADDING = new Insets(5);

    /**
     * 超链接处理器，用于替换默认的打开方式
     */
    private ObjectProperty<Consumer<String>> hyperlinkHandler;

    /**
     * 超链接匹配模式
     */
    private Pattern hyperlinkPattern = DEFAULT_HYPERLINK_PATTERN;

    /**
     * 右键菜单处的超链接
     */
    private String contextHyperlink;

    /**
     * 超链接缓存所在段落
     */
    private int hyperlinkCacheParagraph = -1;

    /**
     * 当前段落的超链接缓存
     */
    private List<Hyperlink> hyperlinkCache = List.of();

    /**
     * 模型监听是否已安装
     */
    private boolean hyperlinkListenerInstalled;

    /**
     * 格式类型监听器
     */
    private final ChangeListener<? super EditorFormatType> formatTypeListener = (observableValue, formatType, t1) -> {
        this.syntaxDecorator.setFormatType(t1);
        if (!this.ignoreChange) {
            this.initTextStyle();
        }
    };

    /**
     * 提示词监听器
     */
    private final ChangeListener<? super Set<String>> promptsListener = (observableValue, formatType, t1) -> {
        this.syntaxDecorator.setPrompts(t1);
        this.initTextStyle();
    };

    /**
     * 高亮监听器
     */
    private final ChangeListener<? super String> highlightListener = (observableValue, formatType, t1) -> {
        ThreadUtil.start(() -> {
            // 获取滚动条值
            Double scrollValue = this.getScrollValue();
            this.syntaxDecorator.setHighlight(t1);
            this.refreshText();
            // 清除高亮的时候滚动到原位置
            if (StringUtil.isEmpty(t1)) {
                this.setScrollValue(scrollValue);
            }
        });

    };

    /**
     * 高亮正则监听器
     */
    private final ChangeListener<? super Boolean> highlightRegexListener = (observableValue, formatType, t1) -> {
        ThreadUtil.start(() -> {
            this.syntaxDecorator.setHighlightRegex(t1);
            this.refreshText();
        });
    };

    /**
     * 高亮全字匹配监听器
     */
    private final ChangeListener<? super Boolean> highlightWholeWordListener = (observableValue, formatType, t1) -> {
        ThreadUtil.start(() -> {
            this.syntaxDecorator.setHighlightWholeWord(t1);
            this.refreshText();
        });
    };

    /**
     * 高亮大小写匹配监听器
     */
    private final ChangeListener<? super Boolean> highlightMacthCaseListener = (observableValue, formatType, t1) -> {
        ThreadUtil.start(() -> {
            this.syntaxDecorator.setHighlightMatchCase(t1);
            this.refreshText();
        });
    };

    /**
     * 字体监听器
     */
    private final ChangeListener<? super Font> fontListener = (observable, oldValue, newValue) -> {
        Font editorFont = this.getEditorFont();
        if (editorFont != null && !FontUtil.isSameFont(editorFont, newValue)) {
            JulLog.info("font:{} editorFont:{}", newValue, editorFont);
            this.changeFont(editorFont);
        }
    };

    {
        NodeManager.init(this);
    }

    /**
     * 初始化编辑器
     */
    private void initEditor() {
        //        // 监听父节点
        //        this.parentProperty().addListener((observable, oldValue, newValue) -> {
        //            if (newValue == null) {
        //                this.destroy();
        //            }
        //        });
        //        // 处理输入法不支持中文的问题
        //        EditorUtil.setupIMESupport(this);
        // 默认自动换行
        this.setWrapText(true);
        // 默认为内容宽高，避免布局问题
        // this.setUseContentWidth(true);
        // this.setUseContentHeight(true);
        // 默认显示行号
        this.setLineNumbersEnabled(true);
        // 默认高亮当前行
        this.setHighlightCurrentParagraph(true);
        // 内容内边距
        this.setContentPadding(DEFAULT_PADDING);
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
        // 提示词变化事件
        this.promptsProperty().addListener(this.promptsListener);
        // 格式变化事件
        this.formatTypeProperty().addListener(this.formatTypeListener);
        // 高亮事件
        this.highlightProperty().addListener(this.highlightListener);
        this.highlightRegexProperty().addListener(this.highlightRegexListener);
        this.highlightWholeWordProperty().addListener(this.highlightWholeWordListener);
        this.highlightMacthCaseProperty().addListener(this.highlightMacthCaseListener);
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            // 仅在打开菜单时解析右键位置的超链接，避免影响正常输入的性能
            Hyperlink hyperlink = this.findHyperlinkAt(e.getScreenX(), e.getScreenY());
            this.contextHyperlink = hyperlink == null ? null : hyperlink.url();
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, e.getScreenX(), e.getScreenY());
            } else {
                this.clearContextMenu();
            }
        });
        // 超链接交互：Ctrl/Command + 左键打开，双击选中（悬停不改变光标）
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onHyperlinkMousePressed);
        // 双击的默认“按词选中”发生在 MOUSE_CLICKED 阶段，需要在这里重新选中链接
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onHyperlinkMouseClicked);
        // 文本变化后超链接缓存失效
        if (!this.hyperlinkListenerInstalled) {
            this.hyperlinkListenerInstalled = true;
            this.getModel().addListener(e -> this.invalidateHyperlinkCache());
        }
        // 自动补全成对符号
        this.addEventFilter(KeyEvent.KEY_TYPED, this::onAutoPair);
        this.addEventFilter(KeyEvent.KEY_PRESSED, this::onAutoPairBackspace);
        // 初始化样式
        this.applyTheme();
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
                String syntaxesName = formatType == null ? null : formatType.getFullSyntaxesName();
                // 如果发生变化
                if (StringUtil.notEquals(this.syntaxesName, syntaxesName)) {
                    String path = "/tm4javafx/grammars/" + syntaxesName;
                    // 切换语法会重置样式提供者内部的分词状态，必须与后台着色任务互斥，
                    // 否则可能用半切换的语法着色，出现整篇文本只有一种颜色的情况
                    synchronized (this.styleProvider) {
                        this.styleProvider.setGrammar(IGrammarSource.fromResource(Editor.class, path));
                        // 加载成功后才记录，避免加载失败后再也不重试
                        this.syntaxesName = syntaxesName;
                    }
                }
            }
            if (this.getSyntaxDecorator() instanceof StatelessSyntaxDecorator d) {
                d.refresh(this.getModel());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 刷新文本
     */
    protected void refreshText() {
        FXUtil.runWait(() -> {
            if (this.getModel() instanceof CodeTextModel model) {
                this.syntaxDecorator.refreshText(model);
            }
        });
    }

    /**
     * 初始化文本样式
     */
    protected void initTextStyle() {
        this.initSyntaxes();
        this.refreshText();
    }

    /**
     * 高效获取纯文本，避免CodeArea导出富文本段落造成额外开销。
     */
    private String plainText() {
        StyledTextModel model = this.getModel();
        if (model == null) {
            return "";
        }
        int paragraphCount = model.size();
        if (paragraphCount == 0) {
            return "";
        }
        String lineEnding = model.getLineEnding().getText();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < paragraphCount; i++) {
            if (i > 0) {
                builder.append(lineEnding);
            }
            builder.append(model.getPlainText(i));
        }
        return builder.toString();
    }

    /**
     * 获取文档长度，不展开富文本段落。
     */
    private int documentLength() {
        StyledTextModel model = this.getModel();
        if (model == null) {
            return 0;
        }
        int length = 0;
        int lineEndingLength = model.getLineEnding().getText().length();
        for (int i = 0; i < model.size(); i++) {
            if (i > 0) {
                length += lineEndingLength;
            }
            length += model.getParagraphLength(i);
        }
        return length;
    }

    /**
     * 文本长度
     */
    private int textLen;

    /**
     * 文本属性
     */
    private StringProperty textProperty;

    /**
     * 模式监听器
     */
    private final StyledTextModel.Listener modelListener = e -> {
        if (e.isEdit()) {
            String text = this.plainText();
            this.textLen = text.length();
            this.textProperty.setValue(text);
        }
    };

    /**
     * 获取文本属性
     *
     * @return 文本属性
     */
    public StringProperty textProperty() {
        if (this.textProperty == null) {
            String text = this.plainText();
            this.textLen = text.length();
            this.textProperty = new SimpleStringProperty(text);
            super.getModel().addListener(this.modelListener);
        }
        return this.textProperty;
    }

    //    private List<ChangeListener<? super String>> textChangeListeners;

    /**
     * 添加文本监听器
     *
     * @param listener 监听器
     */
    public void addTextChangeListener(ChangeListener<String> listener) {
        synchronized (this) {
            ChangeListener<? super String> changeListener = (observable, oldValue, newValue) -> {
                if (!this.ignoreChange) {
                    listener.changed(observable, oldValue, newValue);
                }
            };
            this.textProperty().addListener(changeListener);
            //            if (this.textChangeListeners == null) {
            //                this.textChangeListeners = new ArrayList<>();
            //            }
            //            this.textChangeListeners.add(changeListener);
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

    /**
     * 获取格式类型
     *
     * @return 格式类型
     */
    public EditorFormatType getFormatType() {
        return this.formatTypeProperty == null ? EditorFormatType.RAW : this.formatTypeProperty.get();
    }

    /**
     * 设置格式类型
     *
     * @param formatType 格式类型
     */
    public void setFormatType(EditorFormatType formatType) {
        this.formatTypeProperty().set(formatType);
    }

    /**
     * 获取格式类型属性
     *
     * @return 格式类型属性
     */
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
        // 内容是否未发生变化
        boolean textUnchanged = false;
        try {
            this.ignoreChange = true;
            String data = null;
            if (rawData instanceof CharSequence sequence) {
                data = sequence.toString();
            } else if (rawData instanceof byte[] array) {
                data = new String(array);
            } else if (rawData != null) {
                data = rawData.toString();
            }
            // 设置格式
            this.setFormatType(formatType);
            if (data != null) {
                // 内容未变化时，下面的text()不会触发变更事件，需要记录，稍后强制重新着色
                textUnchanged = StringUtil.equals(this.plainText(), data);
                // 初始化语法格式
                this.initSyntaxes();
                // 设置内容
                this.text(data);
            } else {
                this.clear();
            }
            // 忘记历史
            this.forgetHistory();
        } finally {
            this.ignoreChange = false;
        }
        // 内容没变但格式变了时，text()不会触发行变更事件，这里强制按当前格式重新着色
        if (textUnchanged) {
            this.refreshText();
        }
    }

    /**
     * 设置内容
     *
     * @param text 内容
     */
    public void text(String text) {
        if (StringUtil.notEquals(this.plainText(), text)) {
            FXUtil.runWait(() -> super.setText(text));
        }
    }

    /**
     * 显示检测后的数据
     *
     * @param rawData 显示数据
     * @return 格式类型
     */
    public EditorFormatType showDetectData(Object rawData) {
        // 检测类型
        byte detectType = TextUtil.detectType(rawData);
        // 处理类型
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
        } else if (detectType == 10) {
            formatType = EditorFormatType.PYTHON;
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

    /**
     * 获取提示词属性
     *
     * @return 提示词属性
     */
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
            String str = this.plainText();
            if (StringUtil.isEmpty(str) || StringUtil.equalsAny(str, "\n", "\r\n")) {
                //            if (StringUtil.isEmpty(str) || StringUtil.equalsAny(str, "\n", "\r", "\r\n")) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 高亮
     */
    private StringProperty highlightProperty;

    /**
     * 获取高亮
     *
     * @return 高亮
     */
    public String getHighlight() {
        return this.highlightProperty == null ? null : this.highlightProperty.get();
    }

    /**
     * 设置高亮
     *
     * @param highlight 高亮
     */
    public void setHighlight(String highlight) {
        this.highlightProperty().set(highlight);
    }

    /**
     * 获取高亮属性
     *
     * @return 高亮属性
     */
    public StringProperty highlightProperty() {
        if (this.highlightProperty == null) {
            this.highlightProperty = new SimpleStringProperty();
        }
        return this.highlightProperty;
    }

    /**
     * 高亮正则
     */
    private BooleanProperty highlightRegexProperty;

    /**
     * 是否高亮正则
     *
     * @return 结果
     */
    public boolean isHighlightRegex() {
        return this.highlightRegexProperty != null && this.highlightRegexProperty.get();
    }

    /**
     * 设置高亮正则
     *
     * @param highlightRegex 高亮正则
     */
    public void setHighlightRegex(boolean highlightRegex) {
        this.highlightRegexProperty().set(highlightRegex);
    }

    /**
     * 获取高亮正则属性
     *
     * @return 高亮正则属性
     */
    public BooleanProperty highlightRegexProperty() {
        if (this.highlightRegexProperty == null) {
            this.highlightRegexProperty = new SimpleBooleanProperty();
        }
        return this.highlightRegexProperty;
    }

    /**
     * 高亮匹配大小写
     */
    private BooleanProperty highlightMacthCaseProperty;

    /**
     * 是否高亮匹配大小写
     *
     * @return 结果
     */
    public boolean isHighlightMacthCase() {
        return this.highlightMacthCaseProperty != null && this.highlightMacthCaseProperty.get();
    }

    /**
     * 设置高亮匹配大小写
     *
     * @param highlightMacthCase 匹配大小写
     */
    public void setHighlightMacthCase(boolean highlightMacthCase) {
        this.highlightMacthCaseProperty().set(highlightMacthCase);
    }

    /**
     * 获取高亮匹配大小写属性
     *
     * @return 高亮匹配大小写属性
     */
    public BooleanProperty highlightMacthCaseProperty() {
        if (this.highlightMacthCaseProperty == null) {
            this.highlightMacthCaseProperty = new SimpleBooleanProperty();
        }
        return this.highlightMacthCaseProperty;
    }

    /**
     * 高亮全字匹配
     */
    private BooleanProperty highlightWholeWordProperty;

    /**
     * 是否高亮全字匹配
     *
     * @return 结果
     */
    public boolean isHighlightWholeWord() {
        return this.highlightWholeWordProperty != null && this.highlightWholeWordProperty.get();
    }

    /**
     * 设置高亮全字匹配
     *
     * @param highlightWholeWord 全字匹配
     */
    public void setHighlightWholeWord(boolean highlightWholeWord) {
        this.highlightWholeWordProperty().set(highlightWholeWord);
    }

    /**
     * 获取高亮全字匹配属性
     *
     * @return 高亮全字匹配属性
     */
    public BooleanProperty highlightWholeWordProperty() {
        if (this.highlightWholeWordProperty == null) {
            this.highlightWholeWordProperty = new SimpleBooleanProperty();
        }
        return this.highlightWholeWordProperty;
    }

    /**
     * 自动补全成对符号开关
     */
    private BooleanProperty autoPairEnabledProperty;

    /**
     * 是否开启自动补全成对符号
     *
     * @return 结果
     */
    public boolean isAutoPairEnabled() {
        return this.autoPairEnabledProperty == null || this.autoPairEnabledProperty.get();
    }

    /**
     * 设置自动补全成对符号开关
     *
     * @param enabled 开关
     */
    public void setAutoPairEnabled(boolean enabled) {
        this.autoPairEnabledProperty().set(enabled);
    }

    /**
     * 获取自动补全成对符号属性
     *
     * @return 自动补全成对符号属性
     */
    public BooleanProperty autoPairEnabledProperty() {
        if (this.autoPairEnabledProperty == null) {
            this.autoPairEnabledProperty = new SimpleBooleanProperty(true);
        }
        return this.autoPairEnabledProperty;
    }

    /**
     * 获取文本长度
     *
     * @return 文本长度
     */
    public int getLength() {
        //        String text = this.textProperty().get();
        //        if (text == null) {
        //            return 0;
        //        }
        //        return text.length();
        if (this.textProperty == null) {
            return this.documentLength();
        }
        return this.textLen;
    }

    /**
     * 获取内容，去除前后空格
     *
     * @return 内容
     */
    public String getTextTrim() {
        String text = this.plainText();
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
     * 获取段落长度
     *
     * @param index 段落索引
     * @return 结果
     */
    public int getParagraphLength(int index) {
        return super.getModel().getParagraphLength(index);
    }

    /**
     * 获取位置
     *
     * @param start 开始位置
     * @param end   结束位置
     * @return 位置
     */
    protected EditorTextPos getPosByIndex(int start, int end) {
        TextPos endPos;
        TextPos startPos;
        //        if (end != start) {
        int length = 0;
        int lastLen = 0;
        int endIndex = -1;
        int startIndex = -1;
        int endOffset = -1;
        int startOffset = -1;
        int pCount = super.getParagraphCount();
        for (int i = 0; i < pCount; i++) {
            int len = this.getParagraphLength(i);
            length += len;
            if (startIndex == -1 && length >= start) {
                startIndex = i;
                startOffset = start - lastLen;
                if (startOffset < 0) {
                    startOffset = 0;
                }
            }
            if (length >= end) {
                endIndex = i;
                endOffset = end - lastLen;
                break;
            }
            length += this.lineEndingLength();
            lastLen = length;
        }
        endPos = TextPos.ofLeading(endIndex, endOffset);
        startPos = TextPos.ofLeading(startIndex, startOffset);
        //        } else {
        //            endPos = startPos = this.getPosByIndex(start);
        //        }
        return new EditorTextPos(startPos, endPos);
    }

    //    /**
    //     * 获取位置
    //     *
    //     * @param index 位置
    //     * @return 位置
    //     */
    //    protected TextPos getPosByIndex(int index) {
    //        int length = 0;
    //        int lastLen = 0;
    //        int startIndex = -1;
    //        int startOffset = -1;
    //        int pCount = super.getParagraphCount();
    //        for (int i = 0; i < pCount; i++) {
    //            int len = this.getParagraphLength(i);
    //            length += len + 1;
    //            if (startIndex == -1 && length >= index) {
    //                startIndex = i;
    //                startOffset = index - lastLen;
    //            }
    //            lastLen = length;
    //        }
    //        if (startIndex == -1) {
    ////            // 返回最后一个有效位置而非 ZERO
    ////            int lastPara = super.getParagraphCount() - 1;
    ////            if (lastPara >= 0) {
    ////                return TextPos.ofLeading(lastPara, this.getParagraphLength(lastPara));
    ////            }
    ////            return TextPos.ZERO;
    //            throw new RuntimeException("getPosByIndex fail, index:" + index);
    //        }
    //        return TextPos.ofLeading(startIndex, startOffset);
    //    }

    /**
     * 根据文本位置获取位置
     *
     * @param pos 文本位置
     * @return 位置
     */
    protected int getOffsetByPos(TextPos pos) {
        int length = 0;
        for (int i = 0; i < pos.index(); i++) {
            int len = this.getParagraphLength(i);
            length += len + this.lineEndingLength();
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
        if (start > end) {
            return;
        }
        if (content == null) {
            return;
        }
        try {
            EditorTextPos pos = this.getPosByIndex(start, end);
            super.replaceText(pos.getStart(), pos.getEnd(), content);
        } catch (Exception ex) {
            ex.printStackTrace();
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
            String text = this.plainText();
            //            if (!StringUtil.endsWith(text, System.lineSeparator()) && !StringUtil.startWith(content, System.lineSeparator())) {
            //                content = System.lineSeparator() + content;
            if (!text.isEmpty()
                    && !StringUtil.endsWith(text, this.lineEndingText())
                    && !StringUtil.startWith(content, this.lineEndingText())) {
                content = this.lineEndingText() + content;
            }
            //            if (endLine && !content.endsWith(System.lineSeparator())) {
            //                content += System.lineSeparator();
            if (endLine && !content.endsWith(this.lineEndingText())) {
                content += this.lineEndingText();
            }
            this.appendContent(content);
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
        int lLen = (pos.offset() + "").length();
        Point2D point2D = this.localToScreen(0, 0);
        double fSize = FontUtil.stringWidth("a", this.getFont());
        // // 获取内边距左
        // Insets insets = this.getContentPadding();
        // double insetsLeft = insets == null ? 0 : insets.getLeft();
        // 获取行号宽
        Node leftSide = flow.lookup(".left-side");
        double leftSideW = leftSide == null ? 0 : NodeUtil.getWidth(leftSide);
        // // 获取边框宽
        // Border border = this.getBorder();
        // BorderStroke stroke = border == null ? null : CollectionUtil.getFirst(border.getStrokes());
        // double borderW = stroke == null ? 0 : stroke.getWidths().getLeft();
        // 获取左侧宽 = 行号宽 + 内容长度 * 字符宽
        double leftW = leftSideW + lLen * fSize;
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
        if (start < 0 || end < 0) {
            return;
        }
        EditorTextPos pos = this.getPosByIndex(start, end);
        FXUtil.runWait(() -> {
            this.requestFocus();
            super.select(pos.getStart(), pos.getEnd());
        });
    }

    /**
     * 设置光标位置
     *
     * @param caretPosition 光标位置
     */
    public void positionCaret(int caretPosition) {
        if (caretPosition < 0) {
            caretPosition = 0;
        }
        int len = this.getLength();
        if (caretPosition > len) {
            caretPosition = len;
        }
        this.selectRange(caretPosition, caretPosition);
        // super.requestFocus();
    }

    /**
     * 设置光标位置
     *
     * @param caretPosition 光标位置
     */
    public void positionCaret(TextPos caretPosition) {
        this.select(caretPosition);
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
    public void deleteText(TextPos start, TextPos end) {
        this.replaceText(start, end, "");
        // this.replaceText(start, end, "", true);
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //    /**
    //     * 行号策略
    //     */
    //    private ObjectProperty<EditorLineNumPolicy> lineNumPolicyProperty;
    //
    //    public EditorLineNumPolicy getLineNumPolicy() {
    //        return this.lineNumPolicyProperty == null ? EditorLineNumPolicy.ALWAYS : this.lineNumPolicyProperty.get();
    //    }
    //
    //    public void setLineNumPolicy(EditorLineNumPolicy lineNumPolicy) {
    //        this.lineNumPolicyProperty().set(lineNumPolicy);
    //    }
    //
    //    public ObjectProperty<EditorLineNumPolicy> lineNumPolicyProperty() {
    //        if (this.lineNumPolicyProperty == null) {
    //            this.lineNumPolicyProperty = new SimpleObjectProperty<>(EditorLineNumPolicy.ALWAYS);
    //        }
    //        return this.lineNumPolicyProperty;
    //    }

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
            // 根据主题的明暗模式和颜色相似度解析最接近的编辑器主题
            String path = EditorUtil.resolveThemeResource(style);
            // 设置主题，与后台着色任务互斥，避免切换过程中被并发使用
            synchronized (this.styleProvider) {
                this.styleProvider.setTheme(IThemeSource.fromResource(Editor.class, path));
                // 应用颜色
                StyleHelper.applyThemeSettings(this, this.styleProvider.getThemeSettings());
            }
            // TODO: 修复主题色可能不生效问题
            NodeHelper.processCSS(this);
            //            this.applyCss();
            // 设置光标行颜色
            this.setCaretLineColor(this.defaultCaretLineColor());
            // 设置选区颜色
            this.setSelectionColor(this.defaultSelectionColor());
            // 设置光标颜色
            this.setCaretColor(ThemeManager.currentForegroundColor());
            // 初始化文字样式
            this.initTextStyle();
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("changeTheme error", ex);
        }
    }

    @Override
    public void initNode() {
        FlexAdapter.super.initNode();
        // 初始化编辑器
        this.initEditor();
        // 尝试初始化提示词
        if (CollectionUtil.isNotEmpty(this.getPrompts())) {
            this.setPrompts(this.getPrompts());
        }
        // 尝试初始化高亮
        if (this.getHighlight() != null) {
            this.setHighlight(this.getHighlight());
        }
        // 对预设了编辑器字体的情况下，组织样式字体修改编辑器字体
        this.fontProperty().addListener(this.fontListener);
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
            return 0;
        }
        return this.getOffsetByPos(pos);
    }

    /**
     * 滚动到顶部
     */
    public void scrollToTop() {
        FXUtil.runPulse(this::moveCaretStart);
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
     * 是否已格式化
     */
    private boolean formatted;

    /**
     * 格式化
     */
    public void formatting() {
        String text = this.plainText();
        String text1;
        if (this.formatted) {
            this.formatted = false;
            text1 = EditorFormatter.unformatText(this.getFormatType(), text);
        } else {
            this.formatted = true;
            text1 = EditorFormatter.formatText(this.getFormatType(), text);
        }
        if (StringUtil.notEquals(text, text1)) {
            //            this.formatted = !this.formatted;
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
    public void paste() {
        super.paste();
        this.applyTheme();
    }

    @Override
    public void paste(DataFormat format) {
        super.paste(format);
        this.applyTheme();
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        // 超链接打开
        this.addHyperlinkMenuItems(items);
        // 是否选中内容
        boolean selectedText = this.isSelectedText();

        // 撤销
        MenuItem undo = MenuItemManager.getMenuItem(I18nHelper.undo(), this::undo);
        undo.setDisable(!this.isUndoable() || !this.isEditable() || this.isDisable());
        items.add(undo);

        // 重做
        MenuItem redo = MenuItemManager.getMenuItem(I18nHelper.redo(), this::redo);
        redo.setDisable(!this.isRedoable() || !this.isEditable() || this.isDisable());
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
        MenuItem wordWrap = MenuItemManager.getCheckMenuItem(I18nHelper.wordWrap(), this::wordWrap, this.isWrapText());
        items.add(wordWrap);

        // 移动到文档头
        MenuItem moveToTheBeginningOfTheDocument = MenuItemManager.getMenuItem(I18nHelper.moveToTheBeginningOfTheDocument(), this::moveCaretStart);
        items.add(moveToTheBeginningOfTheDocument);

        // 移动到文档尾
        MenuItem moveToTheEndOfTheDocument = MenuItemManager.getMenuItem(I18nHelper.moveToTheEndOfTheDocument(), this::moveCaretEnd);
        items.add(moveToTheEndOfTheDocument);

        items.add(MenuItemManager.getSeparatorMenuItem());

        // 格式化文档
        MenuItem formattingDocument = MenuItemManager.getMenuItem(I18nHelper.formattingDocument(), this::formatting);
        items.add(formattingDocument);

        // 行号
        if (this.isLineNumbersEnabled()) {
            MenuItem hideLineNum = MenuItemManager.getCheckMenuItem(I18nHelper.showLineNum(), this::hideLineNum, true);
            items.add(hideLineNum);
        } else {
            MenuItem showLineNum = MenuItemManager.getCheckMenuItem(I18nHelper.showLineNum(), this::showLineNum, false);
            items.add(showLineNum);
        }

        return items;
    }

    /**
     * 追加内容
     *
     * @param text 内容
     */
    public void appendContent(String text) {
        FXUtil.runWait(() -> super.appendText(text));
        // super.appendText(text);
    }

    @Override
    public double getFontSize() {
        Font font = ObjectUtil.nullOrElse(this.getEditorFont(), super.getFont());
        return font.getSize();
    }

    @Override
    public void setFontSize(double fontSize) {
        Font editorFont = this.getEditorFont();
        if (editorFont != null) {
            editorFont = FontUtil.newFontBySize(editorFont, fontSize);
            this.setEditorFont(editorFont);
        } else {
            Font font = FontUtil.newFontBySize(super.getFont(), fontSize);
            this.setFont(font);
        }
    }

    @Override
    public String getFontFamily() {
        Font font = ObjectUtil.nullOrElse(this.getEditorFont(), super.getFont());
        return font.getFamily();
    }

    @Override
    public void setFontFamily(String fontFamily) {
        if (StringUtil.isEmpty(fontFamily)) {
            return;
        }
        Font baseFont = ObjectUtil.nullOrElse(this.getEditorFont(), super.getFont());
        Font newFont = FontUtil.newFontByFamily(baseFont, fontFamily);
        if (this.getEditorFont() != null) {
            this.setEditorFont(newFont);
        } else {
            this.setFont(newFont);
        }
    }

    @Override
    public FontWeight getFontWeight() {
        Font font = ObjectUtil.nullOrElse(this.getEditorFont(), super.getFont());
        return FontUtil.getWeight(font);
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        Font editorFont = this.getEditorFont();
        if (editorFont != null) {
            editorFont = FontUtil.newFontByWeight(editorFont, fontWeight);
            this.setEditorFont(editorFont);
        } else {
            Font font = FontUtil.newFontByWeight(super.getFont(), fontWeight);
            this.setFont(font);
        }
    }

    /**
     * 编辑器字体
     */
    private Font editorFont;

    /**
     * 获取编辑器字体(由于会受主题样式字体影响，故用这个参数去覆盖字体参数)
     *
     * @return 编辑器字体
     */
    public Font getEditorFont() {
        return this.editorFont;
    }

    /**
     * 设置编辑器字体
     *
     * @param editorFont 编辑器字体
     */
    public void setEditorFont(Font editorFont) {
        this.editorFont = editorFont;
        this.changeFont(editorFont);
    }

    @Override
    public void changeFont(Font font) {
        Font font1;
        Font editorFont = this.getEditorFont();
        if (editorFont != null) {
            font1 = editorFont;
        } else if (font != null) {
            font1 = font;
        } else {
            font1 = super.getFont();
        }
        // 我也不知道为啥这样写才能生效
        Runnable func = () -> {
            this.setFont(font1);
            this.applyCss();
            this.layout();
        };
        FXUtil.runWait(func);
    }

    @Override
    public void redo() {
        try {
            super.redo();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void undo() {
        try {
            super.undo();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取选区文字
     *
     * @return 结果
     */
    public String getSelectedText() {
        SelectionSegment segment = this.getSelection();
        if (segment == null || segment.isCollapsed()) {
            return "";
        }
        TextPos min = segment.getMin();
        TextPos max = segment.getMax();
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (int i = min.index(); i <= max.index(); i++) {
            String text = this.getPlainText(i);
            if (i == min.index() && i == max.index()) {
                text = text.substring(min.offset(), max.offset());
            } else if (i == min.index()) {
                text = text.substring(min.offset());
            } else if (i == max.index()) {
                text = text.substring(0, max.offset());
            }
            if (first) {
                first = false;
                builder.append(text);
            } else {
                builder.append(this.lineEndingText()).append(text);
            }
        }
        if (builder.isEmpty()) {
            return "";
        }
        return builder.toString();
    }

    /**
     * 获取选区range
     *
     * @return 选区range
     */
    public IndexRange getSelectionRange() {
        SelectionSegment segment = this.getSelection();
        if (segment == null) {
            return null;
        }
        int start = this.getOffsetByPos(segment.getMin());
        int end = this.getOffsetByPos(segment.getMax());
        return new IndexRange(start, end);
    }

    /**
     * 获取选区行
     *
     * @return 选区行列表，key: 行 value: 内容
     */
    public Map<Integer, String> getSelectionLines() {
        SelectionSegment segment = this.getSelection();
        if (segment == null) {
            return Collections.emptyMap();
        }
        TextPos min = segment.getMin();
        TextPos max = segment.getMax();
        Map<Integer, String> lines = new HashMap<>();
        for (int i = min.index(); i <= max.index(); i++) {
            String text = this.getPlainText(i);
            lines.put(i, text);
        }
        return lines;
    }

    /**
     * 获取行数量
     *
     * @return 行数量
     */
    public long lineCount() {
        StyledTextModel model = this.getModel();
        return model == null ? 0 : model.size();
    }

    /**
     * 获取换行文本
     *
     * @return 结果
     */
    public String lineEndingText() {
        return super.getLineEnding().getText();
    }

    /**
     * 获取换行文本长度
     *
     * @return 结果
     */
    public int lineEndingLength() {
        return this.lineEndingText().length();
    }

    // ====== 超链接 ======

    /**
     * 获取超链接处理器属性。
     * <p>
     * 设置后，Ctrl/Command + 左键点击或右键菜单打开超链接时会回调该处理器，
     * 未设置时使用系统默认浏览器打开。
     *
     * @return 超链接处理器属性
     */
    public ObjectProperty<Consumer<String>> hyperlinkHandlerProperty() {
        if (this.hyperlinkHandler == null) {
            this.hyperlinkHandler = new SimpleObjectProperty<>();
        }
        return this.hyperlinkHandler;
    }

    /**
     * 获取超链接处理器
     *
     * @return 超链接处理器
     */
    public Consumer<String> getHyperlinkHandler() {
        return this.hyperlinkHandler == null ? null : this.hyperlinkHandler.get();
    }

    /**
     * 设置超链接处理器
     *
     * @param hyperlinkHandler 超链接处理器
     */
    public void setHyperlinkHandler(Consumer<String> hyperlinkHandler) {
        this.hyperlinkHandlerProperty().set(hyperlinkHandler);
    }

    /**
     * 获取超链接匹配模式
     *
     * @return 超链接匹配模式
     */
    public Pattern getHyperlinkPattern() {
        return this.hyperlinkPattern;
    }

    /**
     * 设置超链接匹配模式
     *
     * @param hyperlinkPattern 超链接匹配模式，为空时使用默认模式
     */
    public void setHyperlinkPattern(Pattern hyperlinkPattern) {
        this.hyperlinkPattern = hyperlinkPattern == null ? DEFAULT_HYPERLINK_PATTERN : hyperlinkPattern;
    }

    /**
     * 获取指定偏移处的超链接。
     * <p>
     * 说明：该方法会从文档开头逐段定位偏移所在段，适合低频调用；鼠标交互内部使用文本位置定位，不会有大文档遍历开销。
     *
     * @param offset 文本偏移
     * @return 超链接
     */
    public Optional<String> getHyperlinkAt(int offset) {
        Hyperlink hyperlink = this.findHyperlinkAtOffset(offset);
        return hyperlink == null ? Optional.empty() : Optional.of(hyperlink.url());
    }

    /**
     * 获取指定偏移处超链接的选中范围。
     *
     * @param offset 文本偏移
     * @return 超链接范围
     */
    public Optional<IndexRange> getHyperlinkRangeAt(int offset) {
        Hyperlink hyperlink = this.findHyperlinkAtOffset(offset);
        if (hyperlink == null) {
            return Optional.empty();
        }
        int paragraphStart = this.getOffsetByPos(TextPos.ofLeading(hyperlink.paragraph(), 0));
        return Optional.of(new IndexRange(paragraphStart + hyperlink.startOffset(),
                paragraphStart + hyperlink.endOffset()));
    }

    /**
     * 添加打开超链接的右键菜单项（仅当右键位置存在超链接时添加）。
     *
     * @param items 菜单项列表
     */
    protected final void addHyperlinkMenuItems(List<MenuItem> items) {
        String link = this.contextHyperlink;
        this.contextHyperlink = null;
        if (StringUtil.isBlank(link)) {
            return;
        }
        items.add(MenuItemManager.getMenuItem(I18nHelper.openAsUrl(), () -> this.openHyperlink(link)));
        items.add(MenuItemManager.getSeparatorMenuItem());
    }

    /**
     * 打开超链接。
     * <p>
     * 优先使用 {@link #setHyperlinkHandler(Consumer)} 绑定的处理器，
     * 未绑定时在后台线程使用系统默认浏览器打开。
     *
     * @param url 超链接
     */
    public void openHyperlink(String url) {
        if (StringUtil.isBlank(url)) {
            return;
        }
        Consumer<String> handler = this.getHyperlinkHandler();
        if (handler != null) {
            handler.accept(url);
            return;
        }
        ThreadUtil.start(() -> this.defaultOpenHyperlink(url));
    }

    /**
     * 使用系统默认方式打开超链接
     *
     * @param url 超链接
     */
    private void defaultOpenHyperlink(String url) {
        try {
            String target = url;
            // 补全缺省协议
            if (!target.matches("(?i)^[a-z][a-z0-9+.-]*:.*")) {
                if (target.indexOf('@') > 0 && target.indexOf('/') < 0) {
                    target = "mailto:" + target;
                } else {
                    target = "https://" + target;
                }
            }
            FXUtil.open(target);
        } catch (Exception ex) {
            JulLog.warn("open hyperlink failed: " + url, ex);
        }
    }

    /**
     * 处理超链接点击：Ctrl/Command + 左键打开，双击选中
     *
     * @param event 鼠标事件
     */
    private void onHyperlinkMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        int clickCount = event.getClickCount();
        boolean open = clickCount == 1 && (event.isControlDown() || event.isMetaDown());
        // 连续快速双击相邻链接时系统可能累加为 3、4 次点击，这里统一按双击处理
        boolean select = clickCount >= 2;
        if (!open && !select) {
            return;
        }
        TextPos pos = this.getTextPosition(event.getScreenX(), event.getScreenY());
        Hyperlink hyperlink = select ? this.findHyperlinkForDoubleClick(pos) : this.findHyperlink(pos);
        if (hyperlink == null) {
            return;
        }
        event.consume();
        if (select) {
            this.requestFocus();
            this.select(hyperlink.start(), hyperlink.end());
        } else {
            this.openHyperlink(hyperlink.url());
        }
    }

    /**
     * 处理双击：默认的按词选中会在 MOUSE_CLICKED 阶段覆盖之前的选区，
     * 这里重新选中超链接并消费事件，避免选中链接的一部分（如 160x60、-）。
     *
     * @param event 鼠标事件
     */
    private void onHyperlinkMouseClicked(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || event.getClickCount() < 2) {
            return;
        }
        TextPos pos = this.getTextPosition(event.getScreenX(), event.getScreenY());
        Hyperlink hyperlink = this.findHyperlinkForDoubleClick(pos);
        if (hyperlink == null) {
            return;
        }
        event.consume();
        this.select(hyperlink.start(), hyperlink.end());
    }

    /**
     * 双击时查找超链接：允许点击位置落在链接尾部的标点/空白上（如链接后面的 “- ”）。
     *
     * @param pos 文本位置
     * @return 超链接
     */
    private Hyperlink findHyperlinkForDoubleClick(TextPos pos) {
        if (pos == null) {
            return null;
        }
        Hyperlink hyperlink = this.findHyperlink(pos);
        if (hyperlink != null) {
            return hyperlink;
        }
        int index = pos.index();
        if (index < 0 || index >= this.getParagraphCount()) {
            return null;
        }
        int paragraphLength = this.getParagraphLength(index);
        if (paragraphLength <= 0 || paragraphLength > HYPERLINK_MAX_PARAGRAPH_LENGTH) {
            return null;
        }
        int local = Math.max(0, Math.min(pos.offset(), paragraphLength));
        String text = this.getModel().getPlainText(index);
        // 向前最多回看 3 个字符，且中间只能是标点或空白
        for (int delta = 1; delta <= 3; delta++) {
            int probe = local - delta;
            if (probe < 0) {
                break;
            }
            Hyperlink candidate = this.findHyperlink(TextPos.ofLeading(index, probe));
            if (candidate == null) {
                continue;
            }
            // 点击位置本身必须是分隔符，避免双击链接后面的单词时误选中链接
            boolean clickOnSeparator = local >= text.length()
                    || !Character.isLetterOrDigit(text.charAt(local));
            boolean gapIsPunctuation = clickOnSeparator;
            for (int i = candidate.endOffset(); i < local && i < text.length() && gapIsPunctuation; i++) {
                char c = text.charAt(i);
                if (!Character.isWhitespace(c) && HYPERLINK_TRAILING_CHARS.indexOf(c) < 0) {
                    gapIsPunctuation = false;
                }
            }
            if (gapIsPunctuation) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 查找鼠标位置处的超链接
     * <p>
     * 注意：{@link #getTextPosition(double, double)} 需要的是屏幕坐标。
     *
     * @param screenX 屏幕x坐标
     * @param screenY 屏幕y坐标
     * @return 超链接
     */
    private Hyperlink findHyperlinkAt(double screenX, double screenY) {
        return this.findHyperlink(this.getTextPosition(screenX, screenY));
    }

    /**
     * 查找文本位置处的超链接。
     * <p>
     * 同一段落的匹配结果会被缓存，鼠标移动时只做区间判断，避免频繁正则匹配。
     *
     * @param pos 文本位置
     * @return 超链接
     */
    private Hyperlink findHyperlink(TextPos pos) {
        if (pos == null) {
            return null;
        }
        int index = pos.index();
        if (index < 0 || index >= this.getParagraphCount()) {
            return null;
        }
        int paragraphLength = this.getParagraphLength(index);
        if (paragraphLength <= 0 || paragraphLength > HYPERLINK_MAX_PARAGRAPH_LENGTH) {
            return null;
        }
        List<Hyperlink> links = this.hyperlinkLinks(index);
        if (links.isEmpty()) {
            return null;
        }
        int local = Math.max(0, Math.min(pos.offset(), paragraphLength));
        for (Hyperlink link : links) {
            if (local >= link.startOffset() && local <= link.endOffset()) {
                return link;
            }
        }
        return null;
    }

    /**
     * 获取指定段落的超链接列表，结果按段落缓存
     *
     * @param index 段落索引
     * @return 超链接列表
     */
    private List<Hyperlink> hyperlinkLinks(int index) {
        if (index == this.hyperlinkCacheParagraph) {
            return this.hyperlinkCache;
        }
        String text = this.getModel().getPlainText(index);
        List<Hyperlink> links = this.scanHyperlinks(index, text);
        this.hyperlinkCacheParagraph = index;
        this.hyperlinkCache = links;
        return links;
    }

    /**
     * 扫描段落中的全部超链接
     *
     * @param index 段落索引
     * @param text  段落文本
     * @return 超链接列表
     */
    private List<Hyperlink> scanHyperlinks(int index, String text) {
        Matcher matcher = this.hyperlinkPattern.matcher(text);
        List<Hyperlink> links = new ArrayList<>();
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            // 去掉链接尾部标点
            while (end > start && HYPERLINK_TRAILING_CHARS.indexOf(text.charAt(end - 1)) >= 0) {
                end--;
            }
            if (end > start) {
                links.add(new Hyperlink(text.substring(start, end), index, start, end));
            }
        }
        return links;
    }

    /**
     * 超链接缓存失效
     */
    private void invalidateHyperlinkCache() {
        this.hyperlinkCacheParagraph = -1;
        if (!this.hyperlinkCache.isEmpty()) {
            this.hyperlinkCache = List.of();
        }
    }

    /**
     * 查找指定偏移处的超链接
     *
     * @param offset 文本偏移
     * @return 超链接
     */
    private Hyperlink findHyperlinkAtOffset(int offset) {
        if (offset < 0) {
            return null;
        }
        int start = 0;
        int count = this.getParagraphCount();
        for (int i = 0; i < count; i++) {
            int length = this.getParagraphLength(i);
            if (offset <= start + length) {
                return this.findHyperlink(TextPos.ofLeading(i, offset - start));
            }
            start += length + this.lineEndingLength();
        }
        return null;
    }

    /**
     * 超链接信息
     *
     * @param url         超链接
     * @param paragraph   段落索引
     * @param startOffset 段内开始偏移
     * @param endOffset   段内结束偏移
     */
    private record Hyperlink(String url, int paragraph, int startOffset, int endOffset) {

        /**
         * 获取开始位置
         *
         * @return 开始位置
         */
        TextPos start() {
            return TextPos.ofLeading(this.paragraph, this.startOffset);
        }

        /**
         * 获取结束位置
         *
         * @return 结束位置
         */
        TextPos end() {
            return TextPos.ofLeading(this.paragraph, this.endOffset);
        }
    }

    // ====== 自动补全成对符号 ======

    /**
     * 处理自动补全成对符号
     */
    private void onAutoPair(KeyEvent e) {
        if (!this.isAutoPairEnabled() || !this.isEditable() || this.isDisable()) {
            return;
        }
        String ch = e.getCharacter();
        if (StringUtil.isEmpty(ch)) {
            return;
        }
        String closeChar = PAIR_MAP.get(ch);
        if (closeChar == null) {
            return;
        }
        //        e.consume();

        SelectionSegment segment = this.getSelection();
        if (segment != null && !segment.isCollapsed()) {
            // 有选区 — 包裹选区
            e.consume();
            TextPos start = segment.getMin();
            TextPos end = segment.getMax();
            String selText = this.getSelectedText();
            super.replaceText(start, end, ch + selText + closeChar);
            TextPos selectionStart = TextPos.ofLeading(start.index(), start.offset() + 1);
            TextPos selectionEnd = start.index() == end.index()
                    ? TextPos.ofLeading(start.index(), end.offset() + 1)
                    : TextPos.ofLeading(end.index(), end.offset());
            this.select(selectionStart, selectionEnd);
        } else {
            // 无选区 — 智能跳过或插入成对符号
            TextPos caret = super.getCaretPosition();
            if (caret == null) {
                return;
            }
            String paragraph = this.getPlainText(caret.index());
            int offset = caret.offset();
            if (offset < paragraph.length() && paragraph.charAt(offset) == closeChar.charAt(0)) {
                e.consume();
                // 光标右边已是结束符，直接跳过
                this.positionCaret(TextPos.ofLeading(caret.index(), offset + 1));
            } else {
                e.consume();
                // 插入成对符号，光标居中
                super.replaceText(caret, caret, ch + closeChar);
                this.positionCaret(TextPos.ofLeading(caret.index(), offset + 1));
            }
        }
    }

    /**
     * 处理退格键删除空配对 — 光标位于 {}、()、[]、""、''、`` 之间时同时删除两个字符
     */
    private void onAutoPairBackspace(KeyEvent e) {
        if (!this.isAutoPairEnabled() || !this.isEditable() || this.isDisable()) {
            return;
        }
        if (e.getCode() != KeyCode.BACK_SPACE) {
            return;
        }
        SelectionSegment segment = this.getSelection();
        if (segment != null && !segment.isCollapsed()) {
            return;
        }

        TextPos caret = super.getCaretPosition();
        if (caret == null) {
            return;
        }
        String paragraph = this.getPlainText(caret.index());
        int offset = caret.offset();
        if (offset <= 0 || offset >= paragraph.length()) {
            return;
        }
        String prevChar = String.valueOf(paragraph.charAt(offset - 1));
        String nextChar = String.valueOf(paragraph.charAt(offset));
        String expectedClosing = PAIR_MAP.get(prevChar);
        if (expectedClosing != null && expectedClosing.equals(nextChar)) {
            e.consume();
            super.replaceText(TextPos.ofLeading(caret.index(), offset - 1),
                    TextPos.ofLeading(caret.index(), offset + 1), "");
        }
    }

    @Override
    public void destroy() {
        this.syntaxDecorator.dispose();
        //        if (this.modelListener != null) {
        //        this.getModel().removeListener(this.modelListener);
        //            this.modelListener = null;
        //        }
        //        if (this.promptsListener != null && this.promptsProperty != null) {
        //            this.promptsProperty.removeListener(this.promptsListener);
        //            this.promptsListener = null;
        //        }
        //        if (this.formatTypeListener != null && this.formatTypeProperty != null) {
        //            this.formatTypeProperty.removeListener(this.formatTypeListener);
        //            this.formatTypeListener = null;
        //        }
        //        if (this.highlightListener != null && this.highlightProperty != null) {
        //            this.highlightProperty.removeListener(this.highlightListener);
        //            this.highlightListener = null;
        //        }
        //        if (this.highlightRegexListener != null && this.highlightRegexProperty != null) {
        //            this.highlightRegexProperty.removeListener(this.highlightRegexListener);
        //            this.highlightRegexListener = null;
        //        }
        //        if (this.highlightMacthCaseListener != null && this.highlightMacthCaseProperty != null) {
        //            this.highlightMacthCaseProperty.removeListener(this.highlightMacthCaseListener);
        //            this.highlightMacthCaseListener = null;
        //        }
        //        if (this.fontListener != null) {
        //            this.fontProperty().removeListener(this.fontListener);
        //            this.fontListener = null;
        //        }
        //        if (CollectionUtil.isNotEmpty(this.textChangeListeners) && this.textProperty != null) {
        //            for (ChangeListener<? super String> changeListener : this.textChangeListeners) {
        //                this.textProperty.removeListener(changeListener);
        //            }
        //            this.textChangeListeners.clear();
        //            this.textChangeListeners = null;
        //        }
        //        if (this.textProperty != null) {
        //            this.textProperty.unbind();
        //            this.textProperty = null;
        //        }
        //        if (this.promptsProperty != null) {
        //            this.promptsProperty.unbind();
        //            this.promptsProperty = null;
        //        }
        //        if (this.formatTypeProperty != null) {
        //            this.formatTypeProperty.unbind();
        //            this.formatTypeProperty = null;
        //        }
        //        if (this.highlightProperty != null) {
        //            this.highlightProperty.unbind();
        //            this.highlightProperty = null;
        //        }
        //        if (this.highlightRegexProperty != null) {
        //            this.highlightRegexProperty.unbind();
        //            this.highlightRegexProperty = null;
        //        }
        //        if (this.highlightMacthCaseProperty != null) {
        //            this.highlightMacthCaseProperty.unbind();
        //            this.highlightMacthCaseProperty = null;
        //        }
        //        this.fontProperty().unbind();
        //        this.leftDecoratorProperty().unbind();
        //        this.rightDecoratorProperty().unbind();
        //        this.highlightCurrentParagraphProperty().unbind();
        //        this.editorFont = null;
        //        this.textFlowModel = null;
        //        this.styleProvider = null;
        //        this.syntaxDecorator = null;
        //        this.richTextAreaModel = null;
        NodeDestroyUtil.destroyNode(this);
        NodeDestroyUtil.destroyObject(this);
    }
}
