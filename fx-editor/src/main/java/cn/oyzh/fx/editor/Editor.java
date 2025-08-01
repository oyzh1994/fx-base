package cn.oyzh.fx.editor;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.RegexHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import cn.oyzh.fx.rich.richtextfx.control.BaseRichTextArea;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;

import java.util.regex.Matcher;

/**
 * 富文本数据文本域
 *
 * @author oyzh
 * @since 2024/04/17
 */
public class Editor extends BaseRichTextArea {

    /**
     * 是否忽略变化
     */
    private boolean ignoreChange;

    /**
     * 数据类型
     */
    private ObjectProperty<EditorFormatType> formatTypeProperty;

    public EditorFormatType getFormatType() {
        return this.formatTypeProperty == null ? EditorFormatType.STRING : this.formatTypeProperty.get();
    }

    public void setFormatType(EditorFormatType formatTypeProperty) {
        this.formatTypeProperty().set(formatTypeProperty);
    }

    public ObjectProperty<EditorFormatType> formatTypeProperty() {
        if (this.formatTypeProperty == null) {
            this.formatTypeProperty = new SimpleObjectProperty<>(EditorFormatType.STRING);
        }
        return formatTypeProperty;
    }

    @Override
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
     * 显示数据
     *
     * @param rawData    显示数据
     * @param formatType 格式类型
     */
    public void showData(Object rawData, EditorFormatType formatType) {
        String promptText = this.getPromptText();
        try {
            this.ignoreChange = true;
            this.disable();
            this.setPromptText(I18nHelper.dataLoading() + "...");
            switch (formatType) {
                case HEX -> this.showHexData(rawData);
                case RAW -> this.showRawData(rawData);
                case XML -> this.showXmlData(rawData);
                case JSON -> this.showJsonData(rawData);
                case HTML -> this.showHtmlData(rawData);
                case YAML -> this.showYamlData(rawData);
                case STRING -> this.showStringData(rawData);
                case BINARY -> this.showBinaryData(rawData);
            }
        } finally {
            this.ignoreChange = false;
            this.setPromptText(promptText);
            this.enable();
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
        } else if (detectType == 2) {
            formatType = EditorFormatType.BINARY;
        } else if (detectType == 3) {
            formatType = EditorFormatType.XML;
        } else if (detectType == 4) {
            formatType = EditorFormatType.HTML;
        } else if (detectType == 5) {
            formatType = EditorFormatType.STRING;
        } else {
            formatType = EditorFormatType.RAW;
        }
        this.showData(rawData, formatType);
        return formatType;
    }

    /**
     * 显示字符串数据
     */
    public void showStringData(Object rawData) {
        String stringData = TextUtil.getStringData(rawData);
        this.setText(stringData);
        this.setFormatType(EditorFormatType.STRING);
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
     * 显示二进制数据
     */
    public void showBinaryData(Object rawData) {
        String binaryData = TextUtil.getBinaryData(rawData);
        this.setText(binaryData);
        this.setFormatType(EditorFormatType.BINARY);
    }

    /**
     * 显示十六进制数据
     */
    public void showHexData(Object rawData) {
        String hexData = TextUtil.getHexData(rawData);
        this.setText(hexData);
        this.setFormatType(EditorFormatType.HEX);
    }

    /**
     * 显示原始数据
     */
    public void showRawData(Object rawData) {
        if (rawData instanceof CharSequence sequence) {
            this.setText(sequence.toString());
            this.setFormatType(EditorFormatType.STRING);
        } else if (rawData instanceof byte[] bytes) {
            this.setText(StringUtil.toBinary(bytes));
            this.setFormatType(EditorFormatType.BINARY);
        } else {
            this.setText(rawData == null ? "" : rawData.toString());
            this.setFormatType(EditorFormatType.RAW);
        }
    }

    @Override
    public void initTextStyle(boolean clear) {
        super.initTextStyle(clear);
        // 高亮模式则跳过
        if (StringUtil.isNotBlank(this.getHighlightText())) {
            return;
        }
        // json
        if (this.getFormatType() == EditorFormatType.JSON) {
            this.applyJsonStyle();
        } else if (this.getFormatType() == EditorFormatType.XML) {// xml
            this.applyXmlStyle();
        } else if (this.getFormatType() == EditorFormatType.HTML) {// html
            this.applyHtmlStyle();
        } else if (this.getFormatType() == EditorFormatType.YAML) {// yaml
            this.applyYamlStyle();
        } else if (this.getFormatType() == EditorFormatType.CSS) {// css
            this.applyCssStyle();
        } else if (this.getFormatType() == EditorFormatType.PROPERTIES) {// properties
            this.applyPropertiesStyle();
        } else if (this.getFormatType() == EditorFormatType.BINARY) {// binary
            this.setStyle(0, this.getLength(), "-fx-fill: #32CD32;");
        } else if (this.getFormatType() == EditorFormatType.HEX) {// hex
            this.setStyle(0, this.getLength(), "-fx-fill: #4682B4;");
        }
        this.forgetHistory();
    }

    public EditorVisibleData getVisibleData() {
        int start = this.firstVisibleParToAllParIndex();
        int end = start + this.lastVisibleParToAllParIndex();
        System.out.println("start=" + start);
        System.out.println("end=" + end);
        int fIndex = 0, lIndex = 0;
        for (int i = 0; i < end; i++) {
            int len = this.getParagraphLength(i);
            // 换行符算1个字符
            if (i < start) {
                fIndex += len + 1;
            }
            lIndex += len + 1;
        }
        System.out.println(fIndex + "--fIndex");
        System.out.println(lIndex + "--lIndex");
        EditorVisibleData visibleData = new EditorVisibleData();
        visibleData.setEndIndex(lIndex);
        visibleData.setStartIndex(fIndex);
        visibleData.setEndParagraph(end);
        visibleData.setStartParagraph(start);
        String text = this.getText();
        int index1 = Math.max(fIndex, 0);
        int index2 = Math.min(lIndex, text.length());
        visibleData.setText(text.substring(index1, index2));
        return visibleData;
    }

    @Override
    public int lastVisibleParToAllParIndex() {
        return getVisibleParagraphs().isEmpty() ? 0 : this.getVisibleParagraphs().size() - 1;
    }

    public static final String KEY_STYLE = "-fx-fill: #1232AC;";

    public static final String VALUE_STYLE = "-fx-fill: #95261F;";

    public static final String BASE_STYLE = "-fx-fill: #800000;";

    public static final String COMMENT_STYLE = "-fx-fill: #8C8C8C;";

    public static final String SYMBOL_STYLE = "-fx-fill: #377B2A;";

    public static final String KEYWORD_STYLE = "-fx-fill: #0000FF;";

    /**
     * 应用json样式
     */
    protected void applyJsonStyle() {
        EditorVisibleData visibleData = this.getVisibleData();
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        // ThreadUtil.start(() -> {
        //     Matcher matcher1 = RegexHelper.jsonSymbolPattern().matcher(text);
        //     while (matcher1.find()) {
        //         RichTextStyle style = new RichTextStyle(matcher1.start(0) + fIndex, matcher1.end(0) + fIndex, SYMBOL_STYLE);
        //         this.setStyle(style);
        //     }
        // });
        // ThreadUtil.start(() -> {
        //     Matcher matcher2 = RegexHelper.jsonKeyPattern().matcher(text);
        //     while (matcher2.find()) {
        //         RichTextStyle style = new RichTextStyle(matcher2.start(1) + fIndex, matcher2.end(1) + fIndex, KEY_STYLE);
        //         this.setStyle(style);
        //     }
        // });
        // ThreadUtil.start(() -> {
        //     Matcher matcher3 = RegexHelper.jsonValuePattern().matcher(text);
        //     while (matcher3.find()) {
        //         RichTextStyle style = new RichTextStyle(matcher3.start(1) + fIndex, matcher3.end(1) + fIndex, VALUE_STYLE);
        //         this.setStyle(style);
        //     }
        // });
        ThreadUtil.start(() -> {
            Matcher matcher = RegexHelper.jsonPattern().matcher(text);
            // 状态标记：明确区分当前解析位置
            boolean inObject = false;    // 是否在对象内（{}中）
            boolean expectKey = false;   // 是否期待键（对象内，逗号/左括号后）
            boolean expectValue = false; // 是否期待值（冒号后）
            while (matcher.find()) {
                // 处理对象开始
                if (matcher.group("braceOpen") != null) {
                    inObject = true;
                    expectKey = true; // 对象内首先期待键
                }
                // 处理对象结束
                else if (matcher.group("braceClose") != null) {
                    inObject = false;
                    expectKey = false;
                    expectValue = false;
                }
                // 处理数组开始
                else if (matcher.group("bracketOpen") != null) {
                    expectValue = true; // 数组内直接期待值
                }
                // 处理数组结束
                else if (matcher.group("bracketClose") != null) {
                    expectValue = inObject; // 数组结束后是否期待值取决于是否在对象内
                }
                // 处理逗号分隔符
                else if (matcher.group("comma") != null) {
                    if (inObject) {
                        expectKey = true;  // 对象内逗号后期待新键
                    } else {
                        expectValue = true; // 数组内逗号后期待新值
                    }
                }
                // 处理冒号（键值分隔）
                else if (matcher.group("colon") != null) {
                    expectKey = false;
                    expectValue = true; // 冒号后必然期待值
                }
                // 处理字符串（根据状态判断是键还是值）
                else if (matcher.group("string") != null) {
                    if (expectKey) {
                        expectKey = false;
                        RichTextStyle style = new RichTextStyle(matcher.start("string") + fIndex, matcher.end("string") + fIndex, KEY_STYLE);
                        this.setStyle(style);
                    } else if (expectValue) {
                        expectValue = false;
                        RichTextStyle style = new RichTextStyle(matcher.start("string") + fIndex, matcher.end("string") + fIndex, VALUE_STYLE);
                        this.setStyle(style);
                    }
                } else if (matcher.group("keyword") != null && expectValue) {  // 处理关键字值
                    RichTextStyle style = new RichTextStyle(matcher.start("keyword") + fIndex, matcher.end("keyword") + fIndex, KEYWORD_STYLE);
                    this.setStyle(style);
                    expectValue = false;
                } else if (matcher.group("number") != null && expectValue) { // 处理数字值
                    RichTextStyle style = new RichTextStyle(matcher.start("number") + fIndex, matcher.end("number") + fIndex, KEYWORD_STYLE);
                    this.setStyle(style);
                    expectValue = false;
                }
            }
        });
    }

    /**
     * 应用xml样式
     */
    protected void applyXmlStyle() {
        EditorVisibleData visibleData = this.getVisibleData();
        String text = visibleData.getText();
        int fIndex = visibleData.getStartIndex();
        ThreadUtil.start(() -> {
            Matcher matcher1 = RegexHelper.xmlPattern().matcher(text);
            while (matcher1.find()) {
                RichTextStyle style = new RichTextStyle(matcher1.start(1) + fIndex, matcher1.end(1) + fIndex, BASE_STYLE);
                this.setStyle(style);
            }
        });
        ThreadUtil.start(() -> {
            Matcher matcher2 = RegexHelper.xmlCommentPattern().matcher(text);
            while (matcher2.find()) {
                RichTextStyle style = new RichTextStyle(matcher2.start(0) + fIndex, matcher2.end(0) + fIndex, COMMENT_STYLE);
                this.setStyle(style);
            }
        });
        // ThreadUtil.start(() -> {
        //     Matcher matcher = RegexHelper.xmlPattern().matcher(text);
        //     while (matcher.find()) {
        //         if (matcher.group("comment") != null) {
        //             RichTextStyle style = new RichTextStyle(matcher.start("comment") + fIndex, matcher.end("comment") + fIndex, COMMENT_STYLE);
        //             this.setStyle(style);
        //         } else if (matcher.group("startTag") != null) {
        //             RichTextStyle style = new RichTextStyle(matcher.start("startTag") + fIndex, matcher.end("startTag") + fIndex, BASE_STYLE);
        //             this.setStyle(style);
        //         } else if (matcher.group("endTag") != null) {
        //             RichTextStyle style = new RichTextStyle(matcher.start("endTag") + fIndex, matcher.end("endTag") + fIndex, BASE_STYLE);
        //             this.setStyle(style);
        //         } else if (matcher.group("selfCloseTag") != null) {
        //             RichTextStyle style = new RichTextStyle(matcher.start("selfCloseTag") + fIndex, matcher.end("selfCloseTag") + fIndex, BASE_STYLE);
        //             this.setStyle(style);
        //         }
        //     }
        // });
        ThreadUtil.start(() -> {
            Matcher matcher3 = RegexHelper.xmlAttributePattern().matcher(text);
            while (matcher3.find()) {
                RichTextStyle style1 = new RichTextStyle(matcher3.start(2) + fIndex, matcher3.end(2) + fIndex, KEY_STYLE);
                this.setStyle(style1);
                // 获取属性值
                int valStart = matcher3.group(4) != null ? matcher3.start(4) : matcher3.start(5);
                int valEnd = matcher3.group(4) != null ? matcher3.end(4) : matcher3.end(5);
                if (valStart >= 0 && valEnd >= 0) {
                    RichTextStyle style2 = new RichTextStyle(valStart + fIndex, valEnd + fIndex, VALUE_STYLE);
                    this.setStyle(style2);
                }
            }
        });
    }

    /**
     * 应用html样式
     */
    protected void applyHtmlStyle() {
        EditorVisibleData visibleData = this.getVisibleData();
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        ThreadUtil.start(() -> {
            Matcher matcher1 = RegexHelper.htmlPattern().matcher(text);
            while (matcher1.find()) {
                RichTextStyle style = new RichTextStyle(matcher1.start(1) + fIndex, matcher1.end(1) + fIndex, BASE_STYLE);
                this.setStyle(style);
            }
        });
        ThreadUtil.start(() -> {
            Matcher matcher2 = RegexHelper.htmlCommentPattern().matcher(text);
            while (matcher2.find()) {
                RichTextStyle style = new RichTextStyle(matcher2.start(0) + fIndex, matcher2.end(0) + fIndex, COMMENT_STYLE);
                this.setStyle(style);
            }
        });
        // ThreadUtil.start(() -> {
        //     Matcher matcher = RegexHelper.htmlPattern().matcher(text);
        //     while (matcher.find()) {
        //         if (matcher.group("comment") != null) {
        //             RichTextStyle style = new RichTextStyle(matcher.start("comment") + fIndex, matcher.end("comment") + fIndex, COMMENT_STYLE);
        //             this.setStyle(style);
        //         } else if (matcher.group("tagOpen") != null) {
        //             RichTextStyle style = new RichTextStyle(matcher.start("tagOpen") + fIndex, matcher.end("tagOpen") + fIndex, BASE_STYLE);
        //             this.setStyle(style);
        //         } else if (matcher.group("tagClose") != null) {
        //             RichTextStyle style = new RichTextStyle(matcher.start("tagClose") + fIndex, matcher.end("tagClose") + fIndex, BASE_STYLE);
        //             this.setStyle(style);
        //         } else if (matcher.group("selfCloseTag") != null) {
        //             RichTextStyle style = new RichTextStyle(matcher.start("selfCloseTag") + fIndex, matcher.end("selfCloseTag") + fIndex, BASE_STYLE);
        //             this.setStyle(style);
        //         }
        //     }
        // });
        ThreadUtil.start(() -> {
            Matcher matcher3 = RegexHelper.htmlAttributePattern().matcher(text);
            while (matcher3.find()) {
                RichTextStyle style1 = new RichTextStyle(matcher3.start(2) + fIndex, matcher3.end(2) + fIndex, KEY_STYLE);
                this.setStyle(style1);
                // 获取属性值
                int valStart = matcher3.group(4) != null ? matcher3.start(4) :
                        matcher3.group(5) != null ? matcher3.start(5) :
                                matcher3.start(6);
                int valEnd = matcher3.group(4) != null ? matcher3.end(4) :
                        matcher3.group(5) != null ? matcher3.end(5) :
                                matcher3.end(6);
                if (valStart >= 0 && valEnd >= 0) {
                    RichTextStyle style2 = new RichTextStyle(valStart + fIndex, valEnd + fIndex, VALUE_STYLE);
                    this.setStyle(style2);
                }
            }
        });
    }

    /**
     * 应用yaml样式
     */
    protected void applyYamlStyle() {
        EditorVisibleData visibleData = this.getVisibleData();
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        ThreadUtil.start(() -> {
            Matcher matcher1 = RegexHelper.yamlPattern().matcher(text);
            while (matcher1.find()) {
                String comment = matcher1.group(1);
                // 独立注释
                if (comment != null) {
                    this.setStyle(new RichTextStyle(matcher1.start(1) + fIndex, matcher1.end(1) + fIndex, COMMENT_STYLE));
                } else {
                    comment = matcher1.group(6);
                    this.setStyle(new RichTextStyle(matcher1.start(4) + fIndex, matcher1.end(4) + fIndex, KEY_STYLE));
                    this.setStyle(new RichTextStyle(matcher1.start(5) + fIndex, matcher1.end(5) + fIndex, VALUE_STYLE));
                    if (comment != null) {
                        this.setStyle(new RichTextStyle(matcher1.start(6) + fIndex - 1, matcher1.end(6) + fIndex, COMMENT_STYLE));
                    }
                }
            }
        });
    }

    /**
     * 应用css样式
     */
    protected void applyCssStyle() {
        EditorVisibleData visibleData = this.getVisibleData();
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        ThreadUtil.start(() -> {
            Matcher matcher1 = RegexHelper.cssPattern().matcher(text);
            while (matcher1.find()) {
                // 分组1：注释
                if (matcher1.group("comment") != null) {
                    this.setStyle(new RichTextStyle(matcher1.start("comment") + fIndex, matcher1.end("comment") + fIndex, COMMENT_STYLE));
                } else if (matcher1.group("string") != null) { // 分组2：字符串
                } else if (matcher1.group("atRule") != null) { // 分组3：规则
                    this.setStyle(new RichTextStyle(matcher1.start("atRule") + fIndex, matcher1.end("atRule") + fIndex, SYMBOL_STYLE));
                } else if (matcher1.group("selector") != null) { // 分组4：选择器块
                    this.setStyle(new RichTextStyle(matcher1.start("selector") + fIndex, matcher1.end("selector") + fIndex, BASE_STYLE));
                } else if (matcher1.group("propName") != null) { // 分组5：属性名
                    this.setStyle(new RichTextStyle(matcher1.start("propName") + fIndex, matcher1.end("propName") + fIndex, KEY_STYLE));
                } else if (matcher1.group("value") != null) {  // 分组6：属性值
                    this.setStyle(new RichTextStyle(matcher1.start("value") + fIndex, matcher1.end("value") + fIndex, VALUE_STYLE));
                } else if (matcher1.group("url") != null) {// 分组7：URL值
                    this.setStyle(new RichTextStyle(matcher1.start("url") + fIndex, matcher1.end("url") + fIndex, VALUE_STYLE));
                } else if (matcher1.group("function") != null) { // 分组8：函数名
                    this.setStyle(new RichTextStyle(matcher1.start("function") + fIndex, matcher1.end("function") + fIndex, KEY_STYLE));
                } else if (matcher1.group("blockEnd") != null) {  // 分组9：块结束符
                    this.setStyle(new RichTextStyle(matcher1.start("blockEnd") + fIndex, matcher1.end("blockEnd") + fIndex, SYMBOL_STYLE));
                } else if (matcher1.group("propEnd") != null) {  // 分组10：属性结束符
                    this.setStyle(new RichTextStyle(matcher1.start("propEnd") + fIndex, matcher1.end("propEnd") + fIndex, SYMBOL_STYLE));
                }
            }
        });
    }

    /**
     * 应用properties样式
     */
    protected void applyPropertiesStyle() {
        EditorVisibleData visibleData = this.getVisibleData();
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        ThreadUtil.start(() -> {
            Matcher matcher1 = RegexHelper.propertiesPattern().matcher(text);
            while (matcher1.find()) {
                if (matcher1.group(1) != null) {
                    this.setStyle(new RichTextStyle(matcher1.start(1) + fIndex, matcher1.end(1) + fIndex, COMMENT_STYLE));
                } else {
                    this.setStyle(new RichTextStyle(matcher1.start(2) + fIndex, matcher1.end(2) + fIndex, KEY_STYLE));
                    this.setStyle(new RichTextStyle(matcher1.start(3) + fIndex, matcher1.end(3) + fIndex, VALUE_STYLE));
                    if (matcher1.group(4) != null) {
                        this.setStyle(new RichTextStyle(matcher1.start(4) + fIndex, matcher1.end(4) + fIndex, COMMENT_STYLE));
                    }
                }
            }
        });
        // ThreadUtil.start(() -> {
        //     List<String> lines = text.lines().toList();
        //     int index = 0;
        //     for (String line : lines) {
        //         if (StringUtil.startWithAny(line.trim(), "#", "!")) {
        //             int start = index + fIndex;
        //             int end = start + line.length() + 1;
        //             this.setStyle(new RichTextStyle(start, end, COMMENT_STYLE));
        //         } else if (StringUtil.contains(line, "#")) {
        //             int start = index + line.indexOf("#") + fIndex;
        //             int end = start + line.length() + 1;
        //             this.setStyle(new RichTextStyle(start, end, COMMENT_STYLE));
        //         } else if (StringUtil.contains(line, "!")) {
        //             int start = index + line.indexOf("!") + fIndex;
        //             int end = start + line.length() + 1;
        //             this.setStyle(new RichTextStyle(start, end, COMMENT_STYLE));
        //         }
        //         index += line.length() + 1;
        //     }
        // });
    }

    private ObjectProperty<EditorLineNumPolicy> lineNumPolicyProperty;

    public EditorLineNumPolicy getLineNumPolicy() {
        return this.lineNumPolicyProperty == null ? EditorLineNumPolicy.AUTO : this.lineNumPolicyProperty.get();
    }

    public void setLineNumPolicy(EditorLineNumPolicy lineNumPolicy) {
        this.lineNumPolicyProperty().set(lineNumPolicy);
    }

    public ObjectProperty<EditorLineNumPolicy> lineNumPolicyProperty() {
        if (this.lineNumPolicyProperty == null) {
            this.lineNumPolicyProperty = new SimpleObjectProperty<>(EditorLineNumPolicy.AUTO);
        }
        return this.lineNumPolicyProperty;
    }

    @Override
    public void initNode() {
        super.initNode();

        // 文字变化事件
        this.addTextChangeListener((observableValue, s, t1) -> {
            if (this.getLineNumPolicy() == EditorLineNumPolicy.AUTO) {
                if (StringUtil.isBlank(t1)) {
                    this.hideLineNum();
                } else {
                    this.showLineNum();
                }
            }
        });

        // 行号策略变化事件
        this.lineNumPolicyProperty().addListener((observableValue, editorLineNumPolicy, t1) -> {
            if (t1 == EditorLineNumPolicy.AUTO) {
                if (this.isEmpty()) {
                    this.hideLineNum();
                } else {
                    this.showLineNum();
                }
            } else if (t1 == EditorLineNumPolicy.NONE) {
                this.hideLineNum();
            } else if (t1 == EditorLineNumPolicy.ALWAYS) {
                this.showLineNum();
            }
        });

        // 格式变化事件
        this.formatTypeProperty().addListener((observableValue, formatType, t1) -> {
            this.initTextStyle();
        });

        // 滚动事件
        this.addEventFilter(ScrollEvent.SCROLL, e -> {
            this.initTextStyle(false);
        });

        // 按键事件
        this.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            // 上、下
            if (e.getCode() == KeyCode.UP
                    || e.getCode() == KeyCode.DOWN) {
                this.initTextStyle(false);
            } else if (e.getCode() == KeyCode.DELETE
                    || e.getCode() == KeyCode.BACK_SPACE) {// 删除、回退
                this.initTextStyle(true);
            } else if (e.getCode() == KeyCode.PAGE_UP
                    || e.getCode() == KeyCode.PAGE_DOWN) {// 上翻页，下翻页
                this.initTextStyleDelay();
            }
        });

        // // 监听宽度变化
        // this.widthProperty().addListener((observableValue, number, t1) -> {
        //     this.initTextStyle(false);
        // });

        // 监听高度变化
        this.heightProperty().addListener((observableValue, number, t1) -> {
            this.initTextStyle(false);
        });
    }
}
