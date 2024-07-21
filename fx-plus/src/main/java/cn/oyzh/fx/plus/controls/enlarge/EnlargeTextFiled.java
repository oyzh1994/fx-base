package cn.oyzh.fx.plus.controls.enlarge;

import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;
import cn.oyzh.fx.plus.controls.textfield.LimitTextField;

/**
 * @author oyzh
 * @since 2024/07/09
 */
public class EnlargeTextFiled extends LimitTextField {

    public EnlargeTextFiled() {
        this.setSkin(new EnlargeTextFiledSkin(this));
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public EnlargeTextFiledSkin skin() {
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
}
