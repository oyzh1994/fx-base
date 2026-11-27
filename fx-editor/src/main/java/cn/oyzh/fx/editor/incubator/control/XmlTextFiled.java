package cn.oyzh.fx.editor.incubator.control;

import cn.oyzh.fx.editor.incubator.EditorFormatType;

/**
 * XML文本输入框。
 *
 * @author oyzh
 * @since 2026-11-27
 */
public class XmlTextFiled extends LongTextFiled {

    @Override
    protected LongTextFiledSkin createDefaultSkin() {
        return new LongTextFiledSkin(this) {
            @Override
            protected EditorFormatType getFormatType() {
                return EditorFormatType.XML;
            }
        };
    }
}
