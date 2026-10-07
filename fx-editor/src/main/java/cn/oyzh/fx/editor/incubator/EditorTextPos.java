package cn.oyzh.fx.editor.incubator;

import jfx.incubator.scene.control.richtext.TextPos;

/**
 * 编辑器文本位置，包含起始位置与结束位置
 *
 * @author oyzh
 * @since 2025-08-14
 */
public class EditorTextPos {

    /**
     * 起始位置
     */
    private TextPos start;

    /**
     * 结束位置
     */
    private TextPos end;

    /**
     * 获取起始位置
     *
     * @return 起始位置
     */
    public TextPos getStart() {
        return start;
    }

    /**
     * 设置起始位置
     *
     * @param start 起始位置
     */
    public void setStart(TextPos start) {
        this.start = start;
    }

    /**
     * 获取结束位置
     *
     * @return 结束位置
     */
    public TextPos getEnd() {
        return end;
    }

    /**
     * 设置结束位置
     *
     * @param end 结束位置
     */
    public void setEnd(TextPos end) {
        this.end = end;
    }

    /**
     * 构造编辑器文本位置
     *
     * @param start 起始位置
     * @param end   结束位置
     */
    public EditorTextPos(TextPos start, TextPos end) {
        this.start = start;
        this.end = end;
    }
}
