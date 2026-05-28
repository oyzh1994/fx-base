package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.FilterTextFieldSkin;
import cn.oyzh.fx.gui.skin.HighlightTextFieldSkin;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.Skin;

/**
 * 过滤文本输入框
 *
 * @author oyzh
 * @since 2026/05/14
 */
public class FilterTextField extends LimitTextField {

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public FilterTextFieldSkin skin() {
        if (this.getSkin() == null) {
            this.setSkin(this.createDefaultSkin());
        }
        return (FilterTextFieldSkin) this.getSkin();
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
        return new FilterTextFieldSkin(this);
    }

}
