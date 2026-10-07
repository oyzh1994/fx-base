package cn.oyzh.fx.editor.incubator.control;

import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.skin.EnlargeTextFiledSkin;
import cn.oyzh.fx.gui.text.field.EnlargeTextFiled;

/**
 * 可放大的编辑器输入框
 *
 * @author oyzh
 * @since 2026-07-23
 */
public class EditorEnlargeTextFiled extends EnlargeTextFiled {

    @Override
    public EditorEnlargeTextFiledSkin skin() {
        return (EditorEnlargeTextFiledSkin) super.skin();
    }

    @Override
    protected EnlargeTextFiledSkin createDefaultSkin() {
        return new EditorEnlargeTextFiledSkin(this);
    }

    /**
     * 设置格式类型
     *
     * @param formatType 格式类型
     */
    public void setFormatType(EditorFormatType formatType){
        this.skin().setFormatType(formatType);
    }
}
