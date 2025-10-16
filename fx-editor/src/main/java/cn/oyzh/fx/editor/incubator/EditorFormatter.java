package cn.oyzh.fx.editor.incubator;

import cn.oyzh.common.json.JSONUtil;

/**
 * 编辑器格式化器
 *
 * @author oyzh
 * @since 2025-08-21
 */
public class EditorFormatter {

    /**
     * 当前实例
     */
    public static final EditorFormatter INSTANCE = new EditorFormatter();

    /**
     * 格式化
     *
     * @param formatType 类型
     * @param text       文本
     * @return 格式化后的文本
     */
    public String format(EditorFormatType formatType, String text) {
        if (formatType == EditorFormatType.JSON) {
            return JSONUtil.toPretty(text);
        }
        return text;
    }

    /**
     * 取消格式化
     *
     * @param formatType 类型
     * @param text       文本
     * @return 格式化后的文本
     */
    public String unformat(EditorFormatType formatType, String text) {
        if (formatType == EditorFormatType.JSON) {
            return JSONUtil.toCompress(text);
        }
        return text;
    }

    /**
     * 格式化文本
     *
     * @param formatType 类型
     * @param text       文本
     * @return 格式化后的文本
     */
    public static String formatText(EditorFormatType formatType, String text) {
        return INSTANCE.format(formatType, text);
    }

    /**
     * 取消格式化文本
     *
     * @param formatType 类型
     * @param text       文本
     * @return 格式化后的文本
     */
    public static String unformatText(EditorFormatType formatType, String text) {
        return INSTANCE.unformat(formatType, text);
    }

}
