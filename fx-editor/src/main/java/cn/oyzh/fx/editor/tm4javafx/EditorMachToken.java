package cn.oyzh.fx.editor.tm4javafx;

import tm4javafx.richtext.StyledToken;

/**
 * @author oyzh
 * @since 2025-08-15
 */
public class EditorMachToken {

    private int start;

    private int end;

    private StyledToken token;

    public EditorMachToken(int start, int end, StyledToken token) {
        this.start = start;
        this.end = end;
        this.token = token;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public StyledToken getToken() {
        return token;
    }

    public void setToken(StyledToken token) {
        this.token = token;
    }
}
