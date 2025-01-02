package cn.oyzh.fx.rich.richtextfx.data;

import cn.oyzh.common.util.RegexHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import cn.oyzh.fx.rich.richtextfx.control.FlexRichTextArea;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 富文本数据文本域
 *
 * @author oyzh
 * @since 2024/04/17
 */

public class RichDataTextArea extends FlexRichTextArea {

    /**
     * 搜索文本
     */
    @Getter
    private String searchText;

    /**
     * 是否忽略变化
     */
    @Getter
    private boolean ignoreChange;

    /**
     * 数据类型
     */
    @Setter
    @Getter
    private RichDataType dataType = RichDataType.STRING;

    /**
     * 实际类型类型
     */
    @Getter
    private RichDataType realType = RichDataType.STRING;

    /**
     * 样式触发边距
     */
    private Map<RichDataType, Integer> styleBound;

    /**
     * 设置边距
     *
     * @param type  类型
     * @param limit 最大限制
     */
    public void setStyleBound(RichDataType type, int limit) {
        if (this.styleBound == null) {
            this.styleBound = new HashMap<>();
        }
        this.styleBound.put(type, limit);
    }

    /**
     * 获取边距
     *
     * @param type 类型
     * @return 最大限制
     */
    public Integer getStyleBound(RichDataType type) {
        if (this.styleBound != null) {
            return this.styleBound.get(type);
        }
        return null;
    }

    /**
     * 检查边距
     *
     * @param type 类型
     * @return 结果
     */
    public boolean checkStyleBound(RichDataType type) {
        Integer bound = this.getStyleBound(type);
        return bound == null || this.getLength() < bound;
    }

    @Override
    public void addTextChangeListener(ChangeListener<String> listener) {
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.ignoreChange) {
                listener.changed(observable, oldValue, newValue);
            }
        });
    }

    /**
     * 显示数据
     *
     * @param dataType 数据类型
     * @param rawData  显示数据
     */
    public void showData(@NonNull RichDataType dataType, Object rawData) {
        RichDataType type = this.dataType;
        this.dataType = dataType;
        this.showData(rawData);
        if (type != dataType) {
            this.forgetHistory();
        }
    }

    /**
     * 显示检测后的数据
     *
     * @param rawData 显示数据
     * @return RichDataType
     */
    public RichDataType showDetectData(Object rawData) {
        // 检测类型
        byte detectType = TextUtil.detectType(rawData);
        RichDataType dataType;
        if (detectType == 1) {
            dataType = RichDataType.JSON;
        } else if (detectType == 2) {
            dataType = RichDataType.BINARY;
        } else if (detectType == 3) {
            dataType = RichDataType.STRING;
        } else {
            dataType = RichDataType.RAW;
        }
        this.showData(dataType, rawData);
        return dataType;
    }

    /**
     * 显示数据
     *
     * @param rawData 显示数据
     */
    public void showData(Object rawData) {
        String promptText = this.getPromptText();
        try {
            this.ignoreChange = true;
            this.disable();
            this.setPromptText(I18nHelper.dataLoading() + "...");
            switch (this.dataType) {
                case HEX -> this.showHexData(rawData);
                case RAW -> this.showRawData(rawData);
                case JSON -> this.showJsonData(rawData);
                case STRING -> this.showStringData(rawData);
                case BINARY -> this.showBinaryData(rawData);
            }
        } finally {
            this.ignoreChange = false;
            this.setPromptText(promptText);
            this.enable();
        }
    }

    @Override
    public synchronized void replaceText(int start, int end, String text) {
        super.replaceText(start, end, text);
        if (this.isEmpty()) {
            this.hideLineNum();
        } else {
            this.showLineNum();
        }
    }

    /**
     * 显示字符串数据
     */
    public void showStringData(Object rawData) {
        String stringData = TextUtil.getStringData(rawData);
        this.setText(stringData);
        this.initTextStyle();
        this.dataType = RichDataType.STRING;
    }

    /**
     * 显示json数据
     */
    public void showJsonData(Object rawData) {
        String jsonData = TextUtil.getJsonData(rawData);
        this.setText(jsonData);
        if (this.checkStyleBound(RichDataType.JSON)) {
            this.initTextStyle();
        } else {
            this.clearTextStyle();
        }
        this.dataType = RichDataType.JSON;
    }

    /**
     * 显示二进制数据
     */
    public void showBinaryData(Object rawData) {
        String binaryData = TextUtil.getBinaryData(rawData);
        this.setText(binaryData);
        if (this.checkStyleBound(RichDataType.BINARY)) {
            this.initTextStyle();
        } else {
            this.clearTextStyle();
        }
        this.dataType = RichDataType.BINARY;
    }

    /**
     * 显示十六进制数据
     */
    public void showHexData(Object rawData) {
        String hexData = TextUtil.getHexData(rawData);
        this.setText(hexData);
        if (this.checkStyleBound(RichDataType.HEX)) {
            this.initTextStyle();
        } else {
            this.clearTextStyle();
        }
        this.dataType = RichDataType.HEX;
    }

    /**
     * 显示原始数据
     */
    public void showRawData(Object rawData) {
        if (rawData instanceof CharSequence sequence) {
            this.setText(sequence.toString());
            this.realType = RichDataType.STRING;
        } else if (rawData instanceof byte[] bytes) {
            this.setText(StringUtil.toBinary(bytes));
            this.realType = RichDataType.BINARY;
        }
        this.initTextStyle();
        this.dataType = RichDataType.RAW;
    }

    /**
     * 搜索正则模式
     *
     * @return 搜索正则模式
     */
    private Pattern searchPattern() {
        return Pattern.compile(this.searchText);
    }

    @Override
    public void initTextStyle() {
        if (!super.checkInvalidStyle()) {
            return;
        }
        super.initTextStyle();
        // 搜索
        if (StringUtil.isNotBlank(this.searchText)) {
            String text = this.getText();
            Matcher matcher = this.searchPattern().matcher(text);
            List<RichTextStyle> styles = new ArrayList<>();
            while (matcher.find()) {
                styles.add(new RichTextStyle(matcher.start(), matcher.end(), "-fx-fill: #FF6600;"));
            }
            this.setStyles(styles);
        } else if (this.dataType == RichDataType.JSON) { // json
            String text = this.getText();
            Matcher matcher1 = RegexHelper.jsonSymbolPattern().matcher(text);
            List<RichTextStyle> styles = new ArrayList<>();
            while (matcher1.find()) {
                styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), "-fx-fill: #4169E1;"));
            }
            Matcher matcher2 = RegexHelper.jsonKeyPattern().matcher(text);
            while (matcher2.find()) {
                styles.add(new RichTextStyle(matcher2.start(), matcher2.end() - 1, "-fx-fill: #EE2C2C;"));
            }
            Matcher matcher3 = RegexHelper.jsonValuePattern().matcher(text);
            while (matcher3.find()) {
                styles.add(new RichTextStyle(matcher3.start(), matcher3.end(), "-fx-fill: green;"));
            }
            this.setStyles(styles);
        } else if (this.dataType == RichDataType.BINARY) {// binary
            this.setStyle(0, this.getLength(), "-fx-fill: #32CD32;");
        } else if (this.dataType == RichDataType.HEX) {// hex
            this.setStyle(0, this.getLength(), "-fx-fill: #4682B4;");
        }
    }

    /**
     * 设置搜索文本
     *
     * @param searchText 搜索文本
     */
    public void setSearchText(String searchText) {
        this.searchText = searchText;
        this.initTextStyle();
    }
}
