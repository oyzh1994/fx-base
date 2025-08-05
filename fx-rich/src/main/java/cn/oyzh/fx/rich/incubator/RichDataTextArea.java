// package cn.oyzh.fx.rich.incubator;
//
// import cn.oyzh.common.util.RegexHelper;
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.common.util.TextUtil;
// import cn.oyzh.fx.rich.RichTextStyle;
// import cn.oyzh.fx.rich.RichDataType;
// import cn.oyzh.i18n.I18nHelper;
// import javafx.beans.value.ChangeListener;
// import javafx.scene.paint.Color;
//
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.regex.Matcher;
//
// /**
// * 富文本数据文本域
// *
// * @author oyzh
// * @since 2024/04/17
// */
//
// public class RichDataTextArea extends BaseRichTextArea {
//
//    /**
//     * 是否忽略变化
//     */
//    private boolean ignoreChange;
//
//    /**
//     * 数据类型
//     */
//    private RichDataType dataType = RichDataType.STRING;
//
//    /**
//     * 实际类型类型
//     */
//    private RichDataType realType = RichDataType.STRING;
//
//    /**
//     * 样式触发边距
//     */
//    private Map<RichDataType, Integer> styleBound;
//
//    public boolean isIgnoreChange() {
//        return ignoreChange;
//    }
//
//    public void setIgnoreChange(boolean ignoreChange) {
//        this.ignoreChange = ignoreChange;
//    }
//
//    public RichDataType getDataType() {
//        return dataType;
//    }
//
//    public void setDataType(RichDataType dataType) {
//        this.dataType = dataType;
//    }
//
//    public RichDataType getRealType() {
//        return realType;
//    }
//
//    public void setRealType(RichDataType realType) {
//        this.realType = realType;
//    }
//
//    public Map<RichDataType, Integer> getStyleBound() {
//        return styleBound;
//    }
//
//    public void setStyleBound(Map<RichDataType, Integer> styleBound) {
//        this.styleBound = styleBound;
//    }
//
//    /**
//     * 设置边距
//     *
//     * @param type  类型
//     * @param limit 最大限制
//     */
//    public void setStyleBound(RichDataType type, int limit) {
//        if (this.styleBound == null) {
//            this.styleBound = new HashMap<>();
//        }
//        this.styleBound.put(type, limit);
//    }
//
//    /**
//     * 获取边距
//     *
//     * @param type 类型
//     * @return 最大限制
//     */
//    public Integer getStyleBound(RichDataType type) {
//        if (this.styleBound != null) {
//            return this.styleBound.get(type);
//        }
//        return null;
//    }
//
//    /**
//     * 检查边距
//     *
//     * @param type 类型
//     * @return 结果
//     */
//    public boolean checkStyleBound(RichDataType type) {
//        Integer bound = this.getStyleBound(type);
//        return bound == null || this.getLength() < bound;
//    }
//
//    @Override
//    public void addTextChangeListener(ChangeListener<String> listener) {
//        synchronized (this) {
//            this.textProperty().addListener((observable, oldValue, newValue) -> {
//                if (!this.ignoreChange) {
//                    listener.changed(observable, oldValue, newValue);
//                }
//            });
//        }
//    }
//
//    /**
//     * 显示数据
//     *
//     * @param dataType 数据类型
//     * @param rawData  显示数据
//     */
//    public void showData(RichDataType dataType, Object rawData) {
//        RichDataType type = this.dataType;
//        this.dataType = dataType;
//        this.showData(rawData);
//        if (type != dataType) {
//            this.forgetHistory();
//        }
//    }
//
//    /**
//     * 显示检测后的数据
//     *
//     * @param rawData 显示数据
//     * @return RichDataType
//     */
//    public RichDataType showDetectData(Object rawData) {
//        // 检测类型
//        byte detectType = TextUtil.detectType(rawData);
//        RichDataType dataType;
//        if (detectType == 1) {
//            dataType = RichDataType.JSON;
//        } else if (detectType == 2) {
//            dataType = RichDataType.BINARY;
//        } else if (detectType == 3) {
//            dataType = RichDataType.XML;
//        } else if (detectType == 4) {
//            dataType = RichDataType.HTML;
//        } else if (detectType == 5) {
//            dataType = RichDataType.STRING;
//        } else {
//            dataType = RichDataType.RAW;
//        }
//        this.showData(dataType, rawData);
//        return dataType;
//    }
//
//    /**
//     * 显示数据
//     *
//     * @param rawData 显示数据
//     */
//    public void showData(Object rawData) {
//        String promptText = this.getPromptText();
//        try {
//            this.ignoreChange = true;
//            this.disable();
//            this.setPromptText(I18nHelper.dataLoading() + "...");
//            switch (this.dataType) {
//                case HEX -> this.showHexData(rawData);
//                case RAW -> this.showRawData(rawData);
//                case XML -> this.showXmlData(rawData);
//                case JSON -> this.showJsonData(rawData);
//                case HTML -> this.showHtmlData(rawData);
//                case YAML -> this.showYamlData(rawData);
//                case STRING -> this.showStringData(rawData);
//                case BINARY -> this.showBinaryData(rawData);
//            }
//        } finally {
//            this.ignoreChange = false;
//            this.setPromptText(promptText);
//            this.enable();
//        }
//    }
//
//    @Override
//    public synchronized void replaceText(int start, int end, String text) {
//        super.replaceText(start, end, text);
//        if (this.isEmpty()) {
//            this.hideLineNum();
//        } else {
//            this.showLineNum();
//        }
//    }
//
//
//    /**
//     * 显示字符串数据
//     */
//    public void showStringData(Object rawData) {
//        String stringData = TextUtil.getStringData(rawData);
//        this.setText(stringData);
//        this.initTextStyle();
//        this.dataType = RichDataType.STRING;
//    }
//
//    /**
//     * 显示json数据
//     */
//    public void showJsonData(Object rawData) {
//        String jsonData = TextUtil.getJsonData(rawData);
//        this.setText(jsonData);
//        if (this.checkStyleBound(RichDataType.JSON)) {
//            this.initTextStyle();
//        } else {
//            this.clearTextStyle();
//        }
//        this.dataType = RichDataType.JSON;
//    }
//
//    /**
//     * 显示xml数据
//     */
//    public void showXmlData(Object rawData) {
//        String xmlData = TextUtil.getXmlData(rawData);
//        this.setText(xmlData);
//        if (this.checkStyleBound(RichDataType.XML)) {
//            this.initTextStyle();
//        } else {
//            this.clearTextStyle();
//        }
//        this.dataType = RichDataType.XML;
//    }
//
//    /**
//     * 显示html数据
//     */
//    public void showHtmlData(Object rawData) {
//        String htmlData = TextUtil.getHtmlData(rawData);
//        this.setText(htmlData);
//        if (this.checkStyleBound(RichDataType.HTML)) {
//            this.initTextStyle();
//        } else {
//            this.clearTextStyle();
//        }
//        this.dataType = RichDataType.HTML;
//    }
//
//    /**
//     * 显示yaml数据
//     */
//    public void showYamlData(Object rawData) {
//        String yamlData = TextUtil.getYamlData(rawData);
//        this.setText(yamlData);
//        if (this.checkStyleBound(RichDataType.YAML)) {
//            this.initTextStyle();
//        } else {
//            this.clearTextStyle();
//        }
//        this.dataType = RichDataType.YAML;
//    }
//
//    /**
//     * 显示二进制数据
//     */
//    public void showBinaryData(Object rawData) {
//        String binaryData = TextUtil.getBinaryData(rawData);
//        this.setText(binaryData);
//        if (this.checkStyleBound(RichDataType.BINARY)) {
//            this.initTextStyle();
//        } else {
//            this.clearTextStyle();
//        }
//        this.dataType = RichDataType.BINARY;
//    }
//
//    /**
//     * 显示十六进制数据
//     */
//    public void showHexData(Object rawData) {
//        String hexData = TextUtil.getHexData(rawData);
//        this.setText(hexData);
//        if (this.checkStyleBound(RichDataType.HEX)) {
//            this.initTextStyle();
//        } else {
//            this.clearTextStyle();
//        }
//        this.dataType = RichDataType.HEX;
//    }
//
//    /**
//     * 显示原始数据
//     */
//    public void showRawData(Object rawData) {
//        if (rawData instanceof CharSequence sequence) {
//            this.setText(sequence.toString());
//            this.realType = RichDataType.STRING;
//        } else if (rawData instanceof byte[] bytes) {
//            this.setText(StringUtil.toBinary(bytes));
//            this.realType = RichDataType.BINARY;
//        }
//        this.initTextStyle();
//        this.dataType = RichDataType.RAW;
//    }
//
//    @Override
//    public void initTextStyle() {
//        super.initTextStyle();
//        // 高亮模式则跳过
//        if (StringUtil.isNotBlank(this.getHighlightText())) {
//            return;
//        }
//        // json
//        if (this.dataType == RichDataType.JSON) {
//            String text = this.getText();
//            List<RichTextStyle> styles = new ArrayList<>();
//            Matcher matcher1 = RegexHelper.jsonSymbolPattern().matcher(text);
//            while (matcher1.find()) {
//                styles.add(new RichTextStyle(matcher1.start(0), matcher1.end(0), "-fx-fill: #4E913F;"));
//            }
//            Matcher matcher2 = RegexHelper.jsonKeyPattern().matcher(text);
//            while (matcher2.find()) {
// //                styles.add(new RichTextStyle(matcher2.start(1), matcher2.end(1), "-fx-fill: #EE2C2C;"));
// //                styles.add(new RichTextStyle(matcher2.start(1), matcher2.end(1) - 1, "-fx-fill: #EE2C2C;"));
//                styles.add(new RichTextStyle(matcher2.start(1) - 1, matcher2.end(1) + 1, "-fx-fill: #22509F;"));
//            }
//            Matcher matcher3 = RegexHelper.jsonValuePattern().matcher(text);
//            while (matcher3.find()) {
//                styles.add(new RichTextStyle(matcher3.start(1), matcher3.end(1), "-fx-fill: #95261F;"));
//            }
//            this.setStyles(styles);
//        } else if (this.dataType == RichDataType.XML) {// xml
//            String text = this.getText();
//            Matcher matcher1 = RegexHelper.xmlPattern().matcher(text);
//            List<RichTextStyle> styles = new ArrayList<>();
//            while (matcher1.find()) {
//                styles.add(new RichTextStyle(matcher1.start(1) - 1, matcher1.end(1) + 1, "-fx-fill: #75140C;"));
//            }
//            Matcher matcher2 = RegexHelper.xmlCommentPattern().matcher(text);
//            while (matcher2.find()) {
//                styles.add(new RichTextStyle(matcher2.start(0), matcher2.end(0), "-fx-fill: #377E22;"));
//            }
//            this.setStyles(styles);
//        } else if (this.dataType == RichDataType.HTML) {// html
//            String text = this.getText();
//            Matcher matcher1 = RegexHelper.htmlPattern().matcher(text);
//            List<RichTextStyle> styles = new ArrayList<>();
//            while (matcher1.find()) {
//                styles.add(new RichTextStyle(matcher1.start(1) - 1, matcher1.end(1) + 1, "-fx-fill: #75140C;"));
//            }
//            Matcher matcher2 = RegexHelper.htmlCommentPattern().matcher(text);
//            while (matcher2.find()) {
//                styles.add(new RichTextStyle(matcher2.start(0), matcher2.end(0), "-fx-fill: #377E22;"));
//            }
//            this.setStyles(styles);
//        } else if (this.dataType == RichDataType.YAML) {// yaml
//            String text = this.getText();
//            Matcher matcher1 = RegexHelper.yamlPattern().matcher(text);
//            List<RichTextStyle> styles = new ArrayList<>();
//            while (matcher1.find()) {
//                String comment = matcher1.group(1);
//                // 独立注释
//                if (comment != null) {
//                    styles.add(new RichTextStyle(matcher1.start(1) - 1, matcher1.end(1), "-fx-fill: #377E22;"));
//                } else {
//                    comment = matcher1.group(6);
//                    styles.add(new RichTextStyle(matcher1.start(4), matcher1.end(4), "-fx-fill: #75140C;"));
//                    styles.add(new RichTextStyle(matcher1.start(5), matcher1.end(5), "-fx-fill: #0000F5;"));
//                    if (comment != null) {
//                        styles.add(new RichTextStyle(matcher1.start(6), matcher1.end(6), "-fx-fill: #377E22;"));
//                    }
//                }
//            }
//            this.setStyles(styles);
//        } else if (this.dataType == RichDataType.BINARY) {// binary
//            this.setStyle(0, this.getLength(), Color.valueOf("#32CD32"));
//        } else if (this.dataType == RichDataType.HEX) {// hex
//            this.setStyle(0, this.getLength(), Color.valueOf("#4682B4"));
//        }
//    }
// }
