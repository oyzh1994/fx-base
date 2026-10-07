package cn.oyzh.fx.editor.incubator;

import tm4javafx.richtext.StyledToken;

/**
 * 编辑器匹配token，记录匹配的起始位置、结束位置及样式token
 *
 * @author oyzh
 * @since 2025-08-15
 */
public record EditorMachToken(int start, int end, StyledToken token) {

//    private int start;
//
//    private int end;
//
//    private StyledToken token;
//
//    public EditorMachToken(int start, int end, StyledToken token) {
//        this.start = start;
//        this.end = end;
//        this.token = token;
//    }
//
//    public int getStart() {
//        return start;
//    }
//
//    public void setStart(int start) {
//        this.start = start;
//    }
//
//    public int getEnd() {
//        return end;
//    }
//
//    public void setEnd(int end) {
//        this.end = end;
//    }
//
//    public StyledToken getToken() {
//        return token;
//    }
//
//    public void setToken(StyledToken token) {
//        this.token = token;
//    }

    /**
     * 获取匹配长度
     *
     * @return 匹配长度
     */
    public int length() {
        return end - start;
    }
}
