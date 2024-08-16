package cn.oyzh.fx.rich.richtextfx.data;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.RegexHelper;
import cn.oyzh.fx.common.util.StringUtil;
import cn.oyzh.fx.common.util.TextUtil;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.richtextfx.RichTextStyle;
import cn.oyzh.fx.rich.richtextfx.control.FlexRichTextArea;
import javafx.beans.value.ChangeListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 富文本数据文本域
 *
 * @author oyzh
 * @since 2024/04/17
 */
@Getter
public class RichDataTextArea extends FlexRichTextArea {

    /**
     * 是否忽略变化
     */
    private boolean ignoreChange;

    /**
     * 数据类型
     */
    @Setter
    private RichDataType dataType = RichDataType.STRING;

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
    public void setText(String text) {
        super.setText(text);
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
        this.initTextStyle();
        this.dataType = RichDataType.JSON;
    }

    /**
     * 显示二进制数据
     */
    public void showBinaryData(Object rawData) {
        String binaryData = TextUtil.getBinaryData(rawData);
        this.setText(binaryData);
        this.initTextStyle();
        this.dataType = RichDataType.BINARY;
    }

    /**
     * 显示十六进制数据
     */
    public void showHexData(Object rawData) {
        String hexData = TextUtil.getHexData(rawData);
        this.setText(hexData);
        this.initTextStyle();
        this.dataType = RichDataType.HEX;
    }

    /**
     * 显示原始数据
     */
    public void showRawData(Object rawData) {
        if (rawData instanceof CharSequence sequence) {
            this.setText(sequence.toString());
        } else if (rawData instanceof byte[] bytes) {
            this.setText(StringUtil.toBinary(bytes));
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
        FXUtil.runWait(() -> {
            this.clearTextStyle();
            // 搜索
            if (StrUtil.isNotBlank(this.searchText)) {
                String text = this.getText();
                Matcher matcher = this.searchPattern().matcher(text);
                List<RichTextStyle> styles = new ArrayList<>();
                while (matcher.find()) {
                    styles.add(new RichTextStyle(matcher.start(), matcher.end(), "-fx-fill: #FF6600;"));
                }
                for (RichTextStyle style : styles) {
                    this.setStyle(style);
                }
            } else if (this.dataType == RichDataType.JSON) { // json
                String text = this.getText();
                Matcher matcher1 = RegexHelper.jsonSymbolPattern().matcher(text);
                List<RichTextStyle> styles = new ArrayList<>();
                while (matcher1.find()) {
                    styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), "-fx-fill: #4169E1;"));
                }
                Matcher matcher2 =  RegexHelper.jsonKeyPattern().matcher(text);
                while (matcher2.find()) {
                    styles.add(new RichTextStyle(matcher2.start(), matcher2.end() - 1, "-fx-fill: #EE2C2C;"));
                }
                Matcher matcher3 =  RegexHelper.jsonValuePattern().matcher(text);
                while (matcher3.find()) {
                    styles.add(new RichTextStyle(matcher3.start(), matcher3.end(), "-fx-fill: green;"));
                }
                for (RichTextStyle style : styles) {
                    this.setStyle(style);
                }
            } else if (this.dataType == RichDataType.BINARY) {// binary
                this.setStyle(0, this.getLength(), "-fx-fill: #32CD32;");
            } else if (this.dataType == RichDataType.HEX) {// hex
                this.setStyle(0, this.getLength(), "-fx-fill: #4682B4;");
            } else {
                super.changeTheme(ThemeManager.currentTheme());
            }
        });
    }

    /**
     * 搜索文本
     */
    private String searchText;

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
