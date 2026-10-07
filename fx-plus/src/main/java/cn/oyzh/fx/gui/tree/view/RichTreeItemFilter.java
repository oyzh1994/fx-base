package cn.oyzh.fx.gui.tree.view;

import java.util.function.Predicate;

/**
 * 富功能树节点过滤器
 *
 * @author oyzh
 * @since 2023-11-10
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

    /**
     * 获取关键字。
     *
     * @return 关键字
     */
    public String getKw() {
        return kw;
    }

    /**
     * 设置关键字。
     *
     * @param kw 关键字
     */
    public void setKw(String kw) {
        this.kw = kw;
    }

    /**
     * 是否匹配大小写。
     *
     * @return 匹配大小写
     */
    public boolean isMatchCase() {
        return matchCase;
    }

    /**
     * 设置匹配大小写。
     *
     * @param matchCase 匹配大小写
     */
    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    /**
     * 是否全词。
     *
     * @return 全词
     */
    public boolean isWholeWord() {
        return wholeWord;
    }

    /**
     * 设置全词。
     *
     * @param wholeWord 全词
     */
    public void setWholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
    }
}
