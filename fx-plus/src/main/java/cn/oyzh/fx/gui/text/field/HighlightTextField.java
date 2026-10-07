package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.HighlightTextFieldSkin;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.Skin;

/**
 * 高亮文本输入框
 *
 * @author oyzh
 * @since 2026/05/14
 */
public class HighlightTextField extends LimitTextField {

    /**
     * 是否正则匹配
     *
     * @return 是否正则匹配
     */
    public boolean isRegex() {
        return this.skin().isRegex();
    }

    /**
     * 设置是否正则匹配
     *
     * @param regex 是否正则匹配
     */
    public void setRegex(boolean regex) {
        this.skin().setRegex(regex);
    }

    /**
     * 获取正则匹配只读属性
     *
     * @return 正则匹配只读属性
     */
    public ReadOnlyBooleanProperty regexPropery() {
        return this.skin().regexPropery();
    }

    /**
     * 是否匹配大小写
     *
     * @return 是否匹配大小写
     */
    public boolean isMatchCase() {
        return this.skin().isMatchCase();
    }

    /**
     * 设置是否匹配大小写
     *
     * @param matchCase 是否匹配大小写
     */
    public void setMatchCase(boolean matchCase) {
        this.skin().setMatchCase(matchCase);
    }

    /**
     * 获取匹配大小写只读属性
     *
     * @return 匹配大小写只读属性
     */
    public ReadOnlyBooleanProperty matchCasePropery() {
        return this.skin().matchCasePropery();
    }

    /**
     * 是否全词匹配
     *
     * @return 是否全词匹配
     */
    public boolean isWholeWord() {
        return this.skin().isWholeWord();
    }

    /**
     * 设置是否全词匹配
     *
     * @param wholeWord 是否全词匹配
     */
    public void setWholeWord(boolean wholeWord) {
        this.skin().setWholeWord(wholeWord);
    }

    /**
     * 获取全词匹配只读属性
     *
     * @return 全词匹配只读属性
     */
    public ReadOnlyBooleanProperty wholeWordPropery() {
        return this.skin().wholeWordPropery();
    }

    @Override
    public HighlightTextFieldSkin skin() {
        return (HighlightTextFieldSkin) super.skin();
    }

    @Override
    protected HighlightTextFieldSkin createDefaultSkin() {
        return new HighlightTextFieldSkin(this);
    }

}
