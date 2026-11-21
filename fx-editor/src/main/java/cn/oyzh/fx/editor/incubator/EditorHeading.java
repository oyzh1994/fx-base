package cn.oyzh.fx.editor.incubator;

/**
 * 编辑器段落标题。
 *
 * @param paragraphIndex 段落索引
 * @param title          标题文本
 * @param styleClass     附加样式类
 * @author oyzh
 * @since 2026-11-21
 */
public record EditorHeading(int paragraphIndex,
                            String title,
                            String styleClass) {

    public EditorHeading {
        if (paragraphIndex < 0) {
            throw new IllegalArgumentException("Invalid heading paragraph index");
        }
        title = title == null ? "" : title;
        styleClass = styleClass == null ? "" : styleClass;
    }
}
