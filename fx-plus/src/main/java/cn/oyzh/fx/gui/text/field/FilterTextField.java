package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.FilterTextFieldSkin;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.Skin;

/**
 * 搜索文本域
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

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterTextFieldSkin(this);
    }

}
