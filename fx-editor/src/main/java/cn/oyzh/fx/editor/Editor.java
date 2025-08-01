package cn.oyzh.fx.editor;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CostUtil;
import cn.oyzh.common.util.RegexHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import cn.oyzh.fx.rich.richtextfx.control.BaseRichTextArea;
import cn.oyzh.i18n.I18nHelper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;
import org.fxmisc.richtext.model.Paragraph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;

/**
 * 编辑器
 *
 * @author oyzh
 * @since 2025/07/30
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
        String promptText = this.getPromptText();
        try {
            this.ignoreChange = true;
            this.disable();
            this.setPromptText(I18nHelper.dataLoading() + "...");
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
     * 样式flag
     */
    private AtomicInteger styleFlag;


    private AtomicBoolean styling = new AtomicBoolean(false);

    private Future<?> task;
    @Override
    public synchronized void initTextStyle(boolean clear) {
        TaskManager.cancel(task);
        // if (styling.get()) {
        //     return;
        // }
        // // 父类处理
        // super.initTextStyle(clear);
        // 更新当前标志位
        if (this.styleFlag == null) {
            this.styleFlag = new AtomicInteger();
        }
        int styleFlag = this.styleFlag.addAndGet(1);

        // // 高亮模式则跳过
        // if (StringUtil.isNotBlank(this.getHighlightText())) {
        //     return;
        // }
        // 获取可视区数据
        EditorVisibleData visibleData = this.getVisibleData();
        if (visibleData.isEmpty()) {
            return;
        }
        styling.set(true);
        // 异步处理样式
        task=    TaskManager.start(() -> {
            try {
                // while (styling.get()){
                //     ThreadUtil.sleep(1);
                // }


                // json
                if (this.getFormatType() == EditorFormatType.JSON) {
                    // this.applyJsonStyle(visibleData, styleFlag);
                    // this.applyJsonStyle();
                } else if (this.getFormatType() == EditorFormatType.XML) {// xml
                    this.applyXmlStyle(visibleData, styleFlag);
                } else if (this.getFormatType() == EditorFormatType.HTML) {// html
                    this.applyHtmlStyle(visibleData, styleFlag);
                } else if (this.getFormatType() == EditorFormatType.YAML) {// yaml
                    this.applyYamlStyle(visibleData, styleFlag);
                } else if (this.getFormatType() == EditorFormatType.CSS) {// css
                    this.applyCssStyle(visibleData, styleFlag);
                } else if (this.getFormatType() == EditorFormatType.PROPERTIES) {// properties
                    this.applyPropertiesStyle(visibleData, styleFlag);
                }
                this.forgetHistory();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                styling.set(false);
            }
        });

    }

    /**
     * 获取可视区数据
     *
     * @return 可视区数据
     */
    public EditorVisibleData getVisibleData() {
        EditorVisibleData visibleData = new EditorVisibleData();
        FXUtil.runWait(() -> {


            int start = this.firstVisibleParToAllParIndex();
            int end = start + this.lastVisibleParToAllParIndex();
            int fIndex = 0, lIndex = 0;
            for (int i = 0; i < end; i++) {
                try {
                    int len = this.getParagraphLength(i);
                    // 换行符算1个字符
                    if (i < start) {
                        fIndex += len + 1;
                    }
                    lIndex += len + 1;
                } catch (IndexOutOfBoundsException ex) {
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            visibleData.setEndIndex(lIndex);
            visibleData.setStartIndex(fIndex);
            visibleData.setEndParagraph(end);
            visibleData.setStartParagraph(start);
            String text = this.getText();
            int index1 = Math.max(fIndex, 0);
            int index2 = Math.min(lIndex, text.length());
            if (index1 > index2) {
                index1 = index2;
            }
            visibleData.setText(text.substring(index1, index2));
        });
        return visibleData;
    }

    @Override
    public int lastVisibleParToAllParIndex() {
        return getVisibleParagraphs().isEmpty() ? 0 : this.getVisibleParagraphs().size() - 1;
    }

    /**
     * 键样式
     */
    public static final String KEY_STYLE = "-fx-fill: #1232AC;";

    /**
     * 值样式
     */
    public static final String VALUE_STYLE = "-fx-fill: #95261F;";

    /**
     * 基础样式
     */
    public static final String BASE_STYLE = "-fx-fill: #800000;";

    /**
     * 注释样式
     */
    public static final String COMMENT_STYLE = "-fx-fill: #8C8C8C;";

    /**
     * 符号样式
     */
    public static final String SYMBOL_STYLE = "-fx-fill: #377B2A;";

    /**
     * 关键字样式
     */
    public static final String KEYWORD_STYLE = "-fx-fill: #0000FF;";

    /**
     * 应用json样式
     *
     * @param visibleData 可视区数据
     * @param flag        标志位
     */
    protected void applyJsonStyle(EditorVisibleData visibleData, int flag) {
        CostUtil.record();
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        // System.out.println(text);
        Matcher matcher = RegexHelper.jsonPattern().matcher(text);
        // 状态标记：明确区分当前解析位置
        boolean inObject = false;    // 是否在对象内（{}中）
        boolean expectKey = false;   // 是否期待键（对象内，逗号/左括号后）
        boolean expectValue = false; // 是否期待值（冒号后）
        List<RichTextStyle> styles = new ArrayList<>();
        while (matcher.find() && this.styleFlag.intValue() == flag) {
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
                    // this.setStyle(style);
                    styles.add(style);
                } else if (expectValue) {
                    expectValue = false;
                    RichTextStyle style = new RichTextStyle(matcher.start("string") + fIndex, matcher.end("string") + fIndex, VALUE_STYLE);
                    // this.setStyle(style);
                    styles.add(style);
                }
            } else if (matcher.group("keyword") != null && expectValue) {  // 处理关键字值
                RichTextStyle style = new RichTextStyle(matcher.start("keyword") + fIndex, matcher.end("keyword") + fIndex, KEYWORD_STYLE);
                // this.setStyle(style);
                styles.add(style);
                expectValue = false;
            } else if (matcher.group("number") != null && expectValue) { // 处理数字值
                RichTextStyle style = new RichTextStyle(matcher.start("number") + fIndex, matcher.end("number") + fIndex, KEYWORD_STYLE);
                // this.setStyle(style);
                styles.add(style);
                expectValue = false;
            }
        }
        this.setStyles(styles);
        CostUtil.printCost();
    }

    /**
     * 应用json样式
     */
    protected void applyJsonStyle() {
        String text = this.getText();
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
                    RichTextStyle style = new RichTextStyle(matcher.start("string"), matcher.end("string"), KEY_STYLE);
                    this.setStyle(style);
                } else if (expectValue) {
                    expectValue = false;
                    RichTextStyle style = new RichTextStyle(matcher.start("string"), matcher.end("string"), VALUE_STYLE);
                    this.setStyle(style);
                }
            } else if (matcher.group("keyword") != null && expectValue) {  // 处理关键字值
                RichTextStyle style = new RichTextStyle(matcher.start("keyword"), matcher.end("keyword"), KEYWORD_STYLE);
                this.setStyle(style);
                expectValue = false;
            } else if (matcher.group("number") != null && expectValue) { // 处理数字值
                RichTextStyle style = new RichTextStyle(matcher.start("number"), matcher.end("number"), KEYWORD_STYLE);
                this.setStyle(style);
                expectValue = false;
            }
        }
    }

    /**
     * 应用xml样式
     *
     * @param visibleData 可视区数据
     * @param flag        标志位
     */
    protected void applyXmlStyle(EditorVisibleData visibleData, int flag) {
        String text = visibleData.getText();
        int fIndex = visibleData.getStartIndex();
        Matcher matcher1 = RegexHelper.xmlPattern().matcher(text);
        while (matcher1.find() && this.styleFlag.intValue() == flag) {
            RichTextStyle style = new RichTextStyle(matcher1.start(1) + fIndex, matcher1.end(1) + fIndex, BASE_STYLE);
            this.setStyle(style);
        }
        Matcher matcher2 = RegexHelper.xmlCommentPattern().matcher(text);
        while (matcher2.find() && this.styleFlag.intValue() == flag) {
            RichTextStyle style = new RichTextStyle(matcher2.start(0) + fIndex, matcher2.end(0) + fIndex, COMMENT_STYLE);
            this.setStyle(style);
        }
        Matcher matcher3 = RegexHelper.xmlAttributePattern().matcher(text);
        while (matcher3.find() && this.styleFlag.intValue() == flag) {
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
    }

    /**
     * 应用html样式
     *
     * @param visibleData 可视区数据
     * @param flag        标志位
     */
    protected void applyHtmlStyle(EditorVisibleData visibleData, int flag) {
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        Matcher matcher1 = RegexHelper.htmlPattern().matcher(text);
        while (matcher1.find() && this.styleFlag.intValue() == flag) {
            RichTextStyle style = new RichTextStyle(matcher1.start(1) + fIndex, matcher1.end(1) + fIndex, BASE_STYLE);
            this.setStyle(style);
        }
        Matcher matcher2 = RegexHelper.htmlCommentPattern().matcher(text);
        while (matcher2.find() && this.styleFlag.intValue() == flag) {
            RichTextStyle style = new RichTextStyle(matcher2.start(0) + fIndex, matcher2.end(0) + fIndex, COMMENT_STYLE);
            this.setStyle(style);
        }
        Matcher matcher3 = RegexHelper.htmlAttributePattern().matcher(text);
        while (matcher3.find() && this.styleFlag.intValue() == flag) {
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
    }

    /**
     * 应用yaml样式
     *
     * @param visibleData 可视区数据
     * @param flag        标志位
     */
    protected void applyYamlStyle(EditorVisibleData visibleData, int flag) {
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        Matcher matcher1 = RegexHelper.yamlPattern().matcher(text);
        while (matcher1.find() && this.styleFlag.intValue() == flag) {
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
    }

    /**
     * 应用css样式
     *
     * @param visibleData 可视区数据
     * @param flag        标志位
     */
    protected void applyCssStyle(EditorVisibleData visibleData, int flag) {
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        Matcher matcher1 = RegexHelper.cssPattern().matcher(text);
        while (matcher1.find() && this.styleFlag.intValue() == flag) {
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
    }

    /**
     * 应用properties样式
     *
     * @param visibleData 可视区数据
     * @param flag        标志位
     */
    protected void applyPropertiesStyle(EditorVisibleData visibleData, int flag) {
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        Matcher matcher1 = RegexHelper.propertiesPattern().matcher(text);
        while (matcher1.find() && this.styleFlag.intValue() == flag) {
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
    }

    @Override
    protected void initHighlightStyle() {
        if (StringUtil.isBlank(this.getHighlightText())) {
            return;
        }
        EditorVisibleData visibleData = this.getVisibleData();
        int fIndex = visibleData.getStartIndex();
        String text = visibleData.getText();
        // 高亮
        Matcher matcher = this.highlightPattern().matcher(text);
        while (matcher.find()) {
            RichTextStyle style = new RichTextStyle(matcher.start() + fIndex, matcher.end() + fIndex, HIGHLIGHT_STYLE);
            this.setStyle(style);
        }
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
            this.lineNumPolicyProperty = new SimpleObjectProperty<>(EditorLineNumPolicy.ALWAYS);
        }
        return this.lineNumPolicyProperty;
    }

    @Override
    public void initNode() {
        super.initNode();

        // 内容变化事件
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

        // // 滚动事件
        // this.addEventFilter(ScrollEvent.SCROLL, e -> {
        //     this.initTextStyle(false);
        // });
        // // 按键事件
        // this.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
        //     // 上、下
        //     if (e.getCode() == KeyCode.UP
        //             || e.getCode() == KeyCode.DOWN) {
        //         this.initTextStyle(false);
        //     } else if (e.getCode() == KeyCode.PAGE_UP
        //             || e.getCode() == KeyCode.PAGE_DOWN) {// 上翻页，下翻页
        //         this.initTextStyleDelay();
        //     }
        // });

        // // 监听宽度变化
        // this.widthProperty().addListener((observableValue, number, t1) -> {
        //     this.initTextStyle(false);
        // });

        // // 监听高度变化
        // this.heightProperty().addListener((observableValue, number, t1) -> {
        // this.initTextStyle(false);
        // });
        //
        this.estimatedScrollYProperty().addListener((observableValue, aDouble, t1) -> {
            this.initTextStyle(false);
        });

        // this.getVisibleParagraphs().addListener((ListChangeListener<Paragraph<String, String, String>>) change -> {
        //     this.initTextStyle(false);
        // });

        // 显示行号
        this.showLineNum();
    }

}
