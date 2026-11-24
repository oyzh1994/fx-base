package cn.oyzh.fx.editor.incubator;

import javafx.scene.paint.Color;

/**
 * 可折叠的编辑器段落范围。
 * <p>
 * 起始段落显示折叠标题，正文段落范围包含起始段落。
 *
 * @param id             折叠标识，用于在文本刷新时保留展开状态
 * @param startParagraph 起始段落
 * @param endParagraph   结束段落，包含该段落
 * @param title          折叠标题
 * @param collapsed      是否折叠
 * @param bodyColor      展开后的正文颜色
 * @param indicatorColor 标题状态指示色，为空表示不显示
 * @author oyzh
 * @since 2026-11-21
 */
public record EditorFold(String id,
                         int startParagraph,
                         int endParagraph,
                         String title,
                         boolean collapsed,
                         Color bodyColor,
                         Color indicatorColor) {

    public EditorFold {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Fold id is required");
        }
        if (startParagraph < 0 || endParagraph < startParagraph) {
            throw new IllegalArgumentException("Invalid fold paragraph range");
        }
        title = title == null ? "" : title;
    }

    /**
     * 创建修改折叠状态后的实例。
     *
     * @param collapsed 是否折叠
     * @return 折叠配置
     */
    public EditorFold withCollapsed(boolean collapsed) {
        return new EditorFold(this.id, this.startParagraph, this.endParagraph,
                this.title, collapsed, this.bodyColor, this.indicatorColor);
    }

    /**
     * 创建折叠配置
     *
     * @param id             折叠标识
     * @param startParagraph 起始段落
     * @param endParagraph   结束段落
     * @param title          折叠标题
     * @param collapsed      是否折叠
     * @param bodyColor      正文颜色
     */
    public EditorFold(String id,
                      int startParagraph,
                      int endParagraph,
                      String title,
                      boolean collapsed,
                      Color bodyColor) {
        this(id, startParagraph, endParagraph, title, collapsed, bodyColor, null);
    }

    /**
     * 创建不含正文颜色的折叠配置。
     *
     * @param id             折叠标识
     * @param startParagraph 起始段落
     * @param endParagraph   结束段落
     * @param title          折叠标题
     * @param collapsed      是否折叠
     */
    public EditorFold(String id,
                      int startParagraph,
                      int endParagraph,
                      String title,
                      boolean collapsed) {
        this(id, startParagraph, endParagraph, title, collapsed, null, null);
    }
}
