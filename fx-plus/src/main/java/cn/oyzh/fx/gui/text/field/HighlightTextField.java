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
     * 当前皮肤
     *
     * @return 皮肤
     */
    public HighlightTextFieldSkin skin() {
        if (this.getSkin() == null) {
            this.setSkin(this.createDefaultSkin());
        }
        return (HighlightTextFieldSkin) this.getSkin();
    }

    public boolean isRegex() {
        return this.skin().isRegex();
    }

    public void setRegex(boolean regex) {
        this.skin().setRegex(regex);
    }

    public ReadOnlyBooleanProperty regexPropery() {
        return this.skin().regexPropery();
    }

    public boolean isMatchCase() {
        return this.skin().isMatchCase();
    }

    public void setMatchCase(boolean matchCase) {
        this.skin().setMatchCase(matchCase);
    }

    public ReadOnlyBooleanProperty matchCasePropery() {
        return this.skin().matchCasePropery();
    }

    public boolean isWholeWord() {
        return this.skin().isWholeWord();
    }

    public void setWholeWord(boolean wholeWord) {
        this.skin().setWholeWord(wholeWord);
    }

    public ReadOnlyBooleanProperty wholeWordPropery() {
        return this.skin().wholeWordPropery();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new HighlightTextFieldSkin(this);
    }

}
