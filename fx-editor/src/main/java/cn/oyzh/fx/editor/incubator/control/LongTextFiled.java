package cn.oyzh.fx.editor.incubator.control;


import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.text.field.LimitTextField;

/**
 * @author oyzh
 * @since 2024/7/21
 */
public class LongTextFiled extends LimitTextField {

    @Override
    public LongTextFiledSkin skin() {
        return (LongTextFiledSkin) super.skin();
    }

    @Override
    protected LongTextFiledSkin createDefaultSkin() {
        return new LongTextFiledSkin(this) {
            @Override
            protected EditorFormatType getFormatType() {
                return EditorFormatType.LOG;
            }
        };
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
    public Object getValue() {
        return this.getText();
    }
}
