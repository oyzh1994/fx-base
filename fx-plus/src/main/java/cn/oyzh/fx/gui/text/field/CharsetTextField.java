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

    {
        this.addItem("");
        // this.addItem(StandardCharsets.UTF_8.displayName().toLowerCase());
        // this.addItem("gbk");
        // this.addItem("gb18030");
        // this.addItem("gb2312");
        // this.addItem(StandardCharsets.ISO_8859_1.displayName().toLowerCase());
        // this.addItem(StandardCharsets.US_ASCII.displayName().toLowerCase());
        for (String charset : this.charsets()) {
            this.addItem(charset);
        }
    }

    private List<String> charsets(){
        List<String> list = new ArrayList<>();
        for (Charset value : Charset.availableCharsets().values()) {
            list.add(value.displayName().toLowerCase());
        }
        return list;
    }

    public void setInitDefault(boolean initDefault) {
        if (initDefault) {
            this.select(Charset.defaultCharset());
            this.setTipText(I18nHelper.charset());
        } else {
            this.clearSelection();
            this.setTipText(null);
        }
    }

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

    public void select(Charset charset) {
        this.selectItem(charset.displayName());
    }

    @Override
    protected void onTextChanged(String newValue) {
        if (!this.isFocused()) {
            return;
        }
        if (this.skin().isTexting()) {
            this.skin().clearTexting();
            return;
        }
        // 移除选区
        this.clearSelection();
        // 隐藏弹窗
        if (StringUtil.isBlank(newValue)) {
            this.setItemList(this.charsets());
            this.skin().hidePopup();
            return;
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
    }
}
