package cn.oyzh.fx.gui.combobox;

import cn.oyzh.common.util.CharsetUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;

import java.nio.charset.Charset;

/**
 * 字符集选择框
 *
 * @author oyzh
 * @since 2022/12/2
 */
public class CharsetComboBox extends FXComboBox<String> {

    /**
     * 设置初始化默认值：为真时选中系统默认字符集，否则清空选择。
     *
     * @param initDefault 是否初始化默认值
     */
    public void setInitDefault(boolean initDefault) {
        if (initDefault) {
            this.select(Charset.defaultCharset());
            this.setTipText(I18nHelper.charset());
        } else {
            this.clearSelection();
            this.setTipText(null);
        }
    }

    /**
     * 是否已初始化默认值
     *
     * @return 始终返回 false
     */
    public boolean isInitDefault() {
        return false;
    }

    /**
     * 获取字符集
     *
     * @return 当前选中的字符集
     */
    public Charset getCharset() {
        String value = this.getValue();
        return StringUtil.isBlank(value) ? CharsetUtil.defaultCharset() : Charset.forName(value);
    }

    /**
     * 获取字符集名称
     *
     * @return 当前选中的字符集名称
     */
    public String getCharsetName() {
        String value = this.getValue();
        return StringUtil.isBlank(value) ? CharsetUtil.defaultCharsetName() : value;
    }

    @Override
    public void select(String charset) {
        this.setIgnoreChanged(true);
        if (StringUtil.isBlank(charset)) {
            this.selectFirst();
        } else {
            charset = charset.toLowerCase().replace("_", "-");
            super.select(charset.toLowerCase());
        }
        this.setIgnoreChanged(false);
    }

    /**
     * 按字符集选中
     *
     * @param charset 字符集
     */
    public void select(Charset charset) {
        this.select(charset.displayName());
    }

    @Override
    public void initNode() {
        this.setPadding(Insets.EMPTY);
        this.addItem("");
        for (Charset charset : Charset.availableCharsets().values()) {
            this.addItem(charset.displayName().toLowerCase());
        }
        super.initNode();
    }
}
