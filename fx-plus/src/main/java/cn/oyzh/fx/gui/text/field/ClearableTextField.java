package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.ClearableTextFieldSkin;
import javafx.scene.control.Skin;

/**
 * 可清除文本域
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class ClearableTextField extends LimitTextField {

    /**
     * 构造可清空文本字段对象。
     */
    public ClearableTextField( ) {
        super();
    }

    /**
     * 构造可清空文本字段对象。
     *
     * @param text 文本
     */
    public ClearableTextField(String text) {
        super.setText(text);
    }

    /**
     * 构造可清空文本字段对象。
     *
     * @param maxLen 最大长度
     */
    public ClearableTextField(Long maxLen) {
        this.setMaxLen(maxLen);
    }

    /**
     * 构造可清空文本字段对象。
     *
     * @param text 文本
     * @param maxLen 最大长度
     */
    public ClearableTextField(String text, Long maxLen) {
        super.setText(text);
        this.setMaxLen(maxLen);
    }

    @Override
    protected ClearableTextFieldSkin createDefaultSkin() {
        return new ClearableTextFieldSkin(this);
    }

}
