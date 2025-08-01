package cn.oyzh.fx.editor;


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
public class EditorFormatTypeComboBox extends FXComboBox<EditorFormatType> implements I18nSelectAdapter<EditorFormatType> {

    {
        NodeManager.init(this);
        this.setTipText(I18nHelper.format());
    }

    /**
     * 获取格式
     *
     * @return 格式
     */
    public EditorFormatType getFormat() {
        EditorFormatType type = this.getSelectedItem();
        return type == null ? EditorFormatType.RAW : type;
    }

    /**
     * 是否xml格式
     *
     * @return 结果
     */
    public boolean isXmlFormat() {
        return EditorFormatType.XML == this.getFormat();
    }

    /**
     * 是否html格式
     *
     * @return 结果
     */
    public boolean isHtmlFormat() {
        return EditorFormatType.HTML == this.getFormat();
    }

    /**
     * 是否yaml格式
     *
     * @return 结果
     */
    public boolean isYamlFormat() {
        return EditorFormatType.YAML == this.getFormat();
    }

    /**
     * 是否raw格式
     *
     * @return 结果
     */
    public boolean isRawFormat() {
        return EditorFormatType.RAW == this.getFormat();
    }

    /**
     * 是否json格式
     *
     * @return 结果
     */
    public boolean isJsonFormat() {
        return EditorFormatType.JSON == this.getFormat();
    }

    // /**
    //  * 是否二进制格式
    //  *
    //  * @return 结果
    //  */
    // public boolean isBinaryFormat() {
    //     return EditorFormatType.BINARY == this.getFormat();
    // }
    //
    // /**
    //  * 是否十六进制格式
    //  *
    //  * @return 结果
    //  */
    // public boolean isHexFormat() {
    //     return EditorFormatType.HEX == this.getFormat();
    // }
    //
    // /**
    //  * 是否字符串格式
    //  *
    //  * @return 结果
    //  */
    // public boolean isStringFormat() {
    //     return EditorFormatType.STRING == this.getFormat();
    // }

    // /**
    //  * 是否java格式
    //  *
    //  * @return 结果
    //  */
    // public boolean isJavaFormat() {
    //     return EditorFormatType.JAVA == this.getFormat();
    // }
    //
    // /**
    //  * 是否python格式
    //  *
    //  * @return 结果
    //  */
    // public boolean isPythonFormat() {
    //     return EditorFormatType.PYTHON == this.getFormat();
    // }
    //
    // /**
    //  * 是否javascript格式
    //  *
    //  * @return 结果
    //  */
    // public boolean isJavaScriptFormat() {
    //     return EditorFormatType.JAVASCRIPT == this.getFormat();
    // }

    // /**
    //  * 选择字符串格式
    //  */
    // public void selectString() {
    //     this.select(EditorFormatType.STRING);
    // }
    //
    // /**
    //  * 选择二进制格式
    //  */
    // public void selectBinary() {
    //     this.select(EditorFormatType.BINARY);
    // }
    //
    // /**
    //  * 选择十六进制格式
    //  */
    // public void selectHex() {
    //     this.select(EditorFormatType.HEX);
    // }

    /**
     * 选择json格式
     */
    public void selectJson() {
        this.select(EditorFormatType.JSON);
    }

    @Override
    public List<EditorFormatType> values(Locale locale) {
        this.clearItems();
        this.addItem(EditorFormatType.RAW);
        this.addItem(EditorFormatType.JSON);
        this.addItem(EditorFormatType.XML);
        this.addItem(EditorFormatType.HTML);
        this.addItem(EditorFormatType.YAML);
        // this.addItem(EditorFormatType.STRING);
        // this.addItem(EditorFormatType.BINARY);
        // this.addItem(EditorFormatType.HEX);
        this.addItem(EditorFormatType.CSS);
        this.addItem(EditorFormatType.PROPERTIES);
        return this.getItems();
    }
}
