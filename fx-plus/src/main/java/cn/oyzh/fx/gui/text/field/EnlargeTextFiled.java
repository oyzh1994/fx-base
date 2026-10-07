package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.EnlargeTextFiledSkin;
import javafx.scene.control.Skin;

/**
 * 可展开文本输入框
 *
 * @author oyzh
 * @since 2024-07-09
 */
public class EnlargeTextFiled extends LimitTextField {

    /**
     * 设置展开宽度
     *
     * @param width 展开宽度
     */
    public void setEnlargeWidth(double width) {
        this.skin().setEnlargeWidth(width);
    }

    /**
     * 获取展开宽度
     *
     * @return 展开宽度
     */
    public double getEnlargeWidth() {
        return this.skin().getEnlargeWidth();
    }

    /**
     * 设置展开高度
     *
     * @param height 展开高度
     */
    public void setEnlargeHeight(double height) {
        this.skin().setEnlargeHeight(height);
    }

    /**
     * 获取展开高度
     *
     * @return 展开高度
     */
    public double getEnlargeHeight() {
        return this.skin().getEnlargeHeight();
    }

    @Override
    public EnlargeTextFiledSkin skin() {
        return (EnlargeTextFiledSkin) super.skin();
    }

    @Override
    protected EnlargeTextFiledSkin createDefaultSkin() {
        return new EnlargeTextFiledSkin(this);
    }
}
