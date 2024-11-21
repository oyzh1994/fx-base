package cn.oyzh.fx.gui.textfield;

import cn.oyzh.fx.gui.skin.EnlargeTextFiledSkin;
import cn.oyzh.fx.plus.controls.textfield.LimitTextField;
import javafx.scene.control.Skin;

/**
 * @author oyzh
 * @since 2024/07/09
 */
public class EnlargeTextFiled extends LimitTextField {

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public EnlargeTextFiledSkin skin() {
        EnlargeTextFiledSkin skin = (EnlargeTextFiledSkin) this.getSkin();
        if (skin == null) {
            this.setSkin(this.createDefaultSkin());
            skin = (EnlargeTextFiledSkin) this.getSkin();
        }
        return skin;
    }

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
    protected Skin<?> createDefaultSkin() {
        return new EnlargeTextFiledSkin(this);
    }
}
