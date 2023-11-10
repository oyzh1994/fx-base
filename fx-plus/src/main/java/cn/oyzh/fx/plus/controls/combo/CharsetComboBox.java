package cn.oyzh.fx.plus.controls.combo;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符集选择框
 *
 * @author oyzh
 * @since 2022/12/2
 */
public class CharsetComboBox extends FlexComboBox<String> {

    {
        this.getItems().add(StandardCharsets.UTF_8.displayName().toLowerCase());
        this.getItems().add("gbk");
        this.getItems().add("gb18030");
        this.getItems().add("gb2312");
        this.getItems().add(StandardCharsets.ISO_8859_1.displayName().toLowerCase());
        this.getItems().add(StandardCharsets.US_ASCII.displayName().toLowerCase());

        this.select(Charset.defaultCharset());
    }

    /**
     * 获取字符集
     *
     * @return 当前选中的字符集
     */
    public Charset getCharset() {
        String value = this.getValue();
        return StrUtil.isBlank(value) ? CharsetUtil.defaultCharset() : Charset.forName(value);
    }

    /**
     * 获取字符集名称
     *
     * @return 当前选中的字符集名称
     */
    public String getCharsetName() {
        String value = this.getValue();
        return StrUtil.isBlank(value) ? CharsetUtil.defaultCharsetName() : value;
    }

    @Override
    public void select(String charset) {
        this.setIgnoreChanged(true);
        if (StrUtil.isBlank(charset) || "跟随系统".equals(charset)) {
            this.selectFirst();
        } else {
            charset = charset.toLowerCase().replace("_", "-");
            super.select(charset.toLowerCase());
        }
        this.setIgnoreChanged(false);
    }

    public void select(Charset charset) {
        this.select(charset.displayName());
    }
}
