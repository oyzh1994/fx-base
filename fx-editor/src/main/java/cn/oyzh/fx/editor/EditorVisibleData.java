package cn.oyzh.fx.editor;

/**
 * @author oyzh
 * @since 2025-07-31
 */
public class EditorVisibleData {

    private String text;
    private int endIndex;
    private int startIndex;
    private int endParagraph;
    private int startParagraph;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndParagraph() {
        return endParagraph;
    }

    public void setEndParagraph(int endParagraph) {
        this.endParagraph = endParagraph;
    }

    public int getStartParagraph() {
        return startParagraph;
    }

    public void setStartParagraph(int startParagraph) {
        this.startParagraph = startParagraph;
    }
}
