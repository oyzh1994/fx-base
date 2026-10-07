package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.util.CharsetUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.i18n.I18nHelper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字符集选择输入框
 *
 * @author oyzh
 * @since 2025/12/11
 */
public class CharsetTextField extends SelectTextFiled<String> {

    /**
     * 获取全部可用字符集名称
     *
     * @return 字符集名称列表
     */
    private List<String> charsets(){
        List<String> list = new ArrayList<>();
        for (Charset value : Charset.availableCharsets().values()) {
            list.add(value.displayName().toLowerCase());
        }
        return list;
    }

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
        String value = this.getSelectedItem();
        return StringUtil.isBlank(value) ? CharsetUtil.defaultCharset() : Charset.forName(value);
    }

    /**
     * 获取字符集名称
     *
     * @return 当前选中的字符集名称
     */
    public String getCharsetName() {
        String value = this.getSelectedItem();
        return StringUtil.isBlank(value) ? CharsetUtil.defaultCharsetName() : value;
    }

    @Override
    public void selectItem(String charset) {
        this.setIgnoreChanged(true);
        if (StringUtil.isBlank(charset)) {
            this.selectFirstItem();
        } else {
            charset = charset.toLowerCase().replace("_", "-");
            super.selectItem(charset.toLowerCase());
        }
        this.setIgnoreChanged(false);
    }

    /**
     * 按字符集选中
     *
     * @param charset 字符集
     */
    public void select(Charset charset) {
        this.selectItem(charset.displayName());
    }

    @Override
    protected boolean onTextChanged(String newValue) {
        if (!super.onTextChanged(newValue)) {
            return false;
        }
        // 隐藏弹窗
        if (StringUtil.isBlank(newValue)) {
            this.setItemList(this.charsets());
            this.skin().showPopup();
            return false;
        }
        // 过滤内容
        List<String> newList = this.charsets().stream()
                .filter(t -> StringUtil.containsIgnoreCase(t, newValue))
                .collect(Collectors.toList());
        // 设置内容
        this.setItemList(newList);
        // 内容为空，隐藏弹窗
        if (newList.isEmpty()) {
            this.skin().hidePopup();
        } else {
            this.skin().showPopup();
        }
        return true;
    }

    @Override
    public void initNode() {
        this.addItem("");
        for (String charset : this.charsets()) {
            this.addItem(charset);
        }
        super.initNode();
    }
}
