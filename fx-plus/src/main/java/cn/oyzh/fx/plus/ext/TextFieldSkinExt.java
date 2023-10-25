package cn.oyzh.fx.plus.ext;

import javafx.beans.InvalidationListener;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;

/**
 * 文本域皮肤扩展
 *
 * @author oyzh
 * @since 2023/10/25
 */
public class TextFieldSkinExt extends TextFieldSkin {

    /**
     * 文本监听器
     */
    protected final InvalidationListener textChanged = observable -> this.updateButtonVisibility();

    /**
     * 焦点监听器
     */
    protected final InvalidationListener focusChanged = textChanged;

    /**
     * 显示监听器
     */
    protected final InvalidationListener visibleChanged = textChanged;

    /**
     * 禁用监听器
     */
    protected final InvalidationListener disableChanged = textChanged;

    /**
     * 修改按钮状态
     */
    protected void updateButtonVisibility() {
    }

    public TextFieldSkinExt(TextField control) {
        super(control);
        // 初始化监听器
        control.textProperty().addListener(this.textChanged);
        control.focusedProperty().addListener(this.focusChanged);
        control.visibleProperty().addListener(this.visibleChanged);
        control.disableProperty().addListener(this.disableChanged);
    }

    @Override
    public void dispose() {
        // 清除监听器
        this.getSkinnable().textProperty().removeListener(this.textChanged);
        this.getSkinnable().focusedProperty().removeListener(this.focusChanged);
        this.getSkinnable().visibleProperty().removeListener(this.visibleChanged);
        this.getSkinnable().disableProperty().removeListener(this.disableChanged);
        super.dispose();
    }

    /**
     * 获取文本
     *
     * @return 文本
     */
    protected String getText() {
        return this.getSkinnable().getText();
    }

    /**
     * 设置文本
     *
     * @param text 文本
     */
    protected void setText(String text) {
        this.getSkinnable().setText(text);
    }
}
