package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.EnlargeTextFiledSkin;
import javafx.scene.control.Skin;

/**
 * @author oyzh
 * @since 2024/07/09
 */
public class EnlargeTextFiled extends LimitTextField {

    public void setEnlargeWidth(double width) {
        this.skin().setEnlargeWidth(width);
    }

    public double getEnlargeWidth() {
        return this.skin().getEnlargeWidth();
    }

    public void setEnlargeHeight(double height) {
        this.skin().setEnlargeHeight(height);
    }

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
