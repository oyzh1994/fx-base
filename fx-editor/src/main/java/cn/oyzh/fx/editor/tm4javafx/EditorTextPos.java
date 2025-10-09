package cn.oyzh.fx.editor.tm4javafx;

import jfx.incubator.scene.control.richtext.TextPos;

/**
 * @author oyzh
 * @since 2025-08-14
 */
public class EditorTextPos {

    private TextPos start;

    private TextPos end;

    public TextPos getStart() {
        return start;
    }

    public void setStart(TextPos start) {
        this.start = start;
    }

    public TextPos getEnd() {
        return end;
    }

    public void setEnd(TextPos end) {
        this.end = end;
    }

    public EditorTextPos(TextPos start, TextPos end) {
        this.start = start;
        this.end = end;
    }
}
