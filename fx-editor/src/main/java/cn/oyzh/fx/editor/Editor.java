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

    public static final String BASE_STYLE = "-fx-fill: #1232AC;";

    public static final String COMMENT_STYLE = "-fx-fill: #8C8C8C;";

    public static final String SYMBOL_STYLE = "-fx-fill: #377B2A;";

    /**
     * 应用json样式
     */
    protected void applyJsonStyle() {
        EditorVisibleData visibleData = this.getVisibleData();
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        ThreadUtil.start(() -> {
            Matcher matcher1 = RegexHelper.jsonSymbolPattern().matcher(text);
            while (matcher1.find()) {
                RichTextStyle style = new RichTextStyle(matcher1.start(0) + fIndex, matcher1.end(0) + fIndex, SYMBOL_STYLE);
                this.setStyle(style);
            }
        });
        ThreadUtil.start(() -> {
            Matcher matcher2 = RegexHelper.jsonKeyPattern().matcher(text);
            while (matcher2.find()) {
                RichTextStyle style = new RichTextStyle(matcher2.start(1) + fIndex, matcher2.end(1) + fIndex, KEY_STYLE);
                this.setStyle(style);
            }
        });
        ThreadUtil.start(() -> {
            Matcher matcher3 = RegexHelper.jsonValuePattern().matcher(text);
            while (matcher3.find()) {
                RichTextStyle style = new RichTextStyle(matcher3.start(1) + fIndex, matcher3.end(1) + fIndex, VALUE_STYLE);
                this.setStyle(style);
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
        ThreadUtil.start(() -> {
            Matcher matcher3 = RegexHelper.htmlAttributePattern().matcher(text);
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
    //
    // /**
    //  * 应用html样式
    //  */
    // protected void applyHtmlStyle1() {
    //     EditorVisibleData visibleData = this.getVisibleData();
    //     int fIndex = visibleData.getStartIndex();
    //     String text = visibleData.getText();
    //     ThreadUtil.start(() -> {
    //         HtmlResolver syntax = new HtmlResolver(new HtmlResolveListener() {
    //             @Override
    //             public void onTagStart(String tagName, int index) {
    //                 this.onTagEnd(tagName, index);
    //             }
    //
    //             @Override
    //             public void onTagEnd(String tagName, int index) {
    //                 RichTextStyle style = new RichTextStyle(index + fIndex - tagName.length() - 1, index + fIndex, BASE_STYLE);
    //                 setStyle(style);
    //             }
    //
    //             @Override
    //             public void onComment(String comment, int index) {
    //                 RichTextStyle style = new RichTextStyle(index + fIndex - comment.length() - 1, index + fIndex, COMMENT_STYLE);
    //                 setStyle(style);
    //             }
    //
    //             @Override
    //             public void onAttribute(String attrName, String attrValue, int index) {
    //                 RichTextStyle style1 = new RichTextStyle(index + fIndex - attrName.length() - 1, index + fIndex, KEY_STYLE);
    //                 setStyle(style1);
    //                 if (attrValue != null) {
    //                     RichTextStyle style2 = new RichTextStyle(index + fIndex - attrValue.length() - 1, index + fIndex, VALUE_STYLE);
    //                     setStyle(style2);
    //                 }
    //             }
    //         });
    //         syntax.parse(text);
    //     });
    // }

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

        // 监听宽度变化
        this.widthProperty().addListener((observableValue, number, t1) -> {
            this.initTextStyle(false);
        });

        // 监听高度变化
        this.heightProperty().addListener((observableValue, number, t1) -> {
            this.initTextStyle(false);
        });
    }
}
