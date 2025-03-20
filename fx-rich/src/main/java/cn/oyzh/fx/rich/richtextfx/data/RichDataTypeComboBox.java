package cn.oyzh.fx.rich.richtextfx.data;


import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;
import java.util.Locale;

/**
 * redis格式下拉框
 *
 * @author oyzh
 * @since 2023/8/14
 */
public class RichDataTypeComboBox extends FXComboBox<RichDataType> implements I18nSelectAdapter<RichDataType> {

    {
        NodeManager.init(this);
        this.setTipText(I18nHelper.format());
    }

    /**
     * 获取格式
     *
     * @return 格式
     */
    public RichDataType getFormat() {
        RichDataType type = this.getSelectedItem();
        return type == null ? RichDataType.RAW : type;
    }

    /**
     * 是否xml格式
     *
     * @return 结果
     */
    public boolean isXmlFormat() {
        return RichDataType.XML == this.getFormat();
    }

    /**
     * 是否html格式
     *
     * @return 结果
     */
    public boolean isHtmlFormat() {
        return RichDataType.HTML == this.getFormat();
    }

    /**
     * 是否yaml格式
     *
     * @return 结果
     */
    public boolean isYamlFormat() {
        return RichDataType.YAML == this.getFormat();
    }

    /**
     * 是否raw格式
     *
     * @return 结果
     */
    public boolean isRawFormat() {
        return RichDataType.RAW == this.getFormat();
    }

    /**
     * 是否json格式
     *
     * @return 结果
     */
    public boolean isJsonFormat() {
        return RichDataType.JSON == this.getFormat();
    }

    /**
     * 是否二进制格式
     *
     * @return 结果
     */
    public boolean isBinaryFormat() {
        return RichDataType.BINARY == this.getFormat();
    }

    /**
     * 是否十六进制格式
     *
     * @return 结果
     */
    public boolean isHexFormat() {
        return RichDataType.HEX == this.getFormat();
    }

    /**
     * 是否字符串格式
     *
     * @return 结果
     */
    public boolean isStringFormat() {
        return RichDataType.STRING == this.getFormat();
    }

    /**
     * 选择字符串格式
     */
    public void selectString() {
        this.select(RichDataType.STRING);
    }

    /**
     * 选择二进制格式
     */
    public void selectBinary() {
        this.select(RichDataType.BINARY);
    }

    /**
     * 选择十六进制格式
     */
    public void selectHex() {
        this.select(RichDataType.HEX);
    }

    /**
     * 选择json格式
     */
    public void selectJson() {
        this.select(RichDataType.JSON);
    }

    @Override
    public List<RichDataType> values(Locale locale) {
        this.clearItems();
        for (RichDataType value : RichDataType.values()) {
            this.addItem(value);
        }
        return this.getItems();
    }
}
