package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.EnlargeTextFiledSkin;
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
        if (this.getSkin() == null) {
            this.setSkin(this.createDefaultSkin());
        }
        return (EnlargeTextFiledSkin) this.getSkin();
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
