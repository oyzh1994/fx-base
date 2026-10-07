package cn.oyzh.fx.editor.incubator.control;


import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.text.field.LimitTextField;

/**
 * 长文本输入框
 *
 * @author oyzh
 * @since 2026-08-26
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
    public Object getValue() {
        return this.getText();
    }
}
