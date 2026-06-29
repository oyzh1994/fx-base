package cn.oyzh.fx.gui.tree.view;

import java.util.function.Predicate;

/**
 * 富功能树节点过滤器
 *
 * @author oyzh
 * @since 2023/11/10
 */
public abstract class RichTreeItemFilter implements Predicate<RichTreeItem<?>> {

    /**
     * 关键字
     */
    private String kw;

    /**
     * 匹配大小写
     */
    private boolean matchCase;

    /**
     * 全字模式
     */
    private boolean wholeWord;

    public String getKw() {
        return kw;
    }

    public void setKw(String kw) {
        this.kw = kw;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isWholeWord() {
        return wholeWord;
    }

    public void setWholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
    }
}
