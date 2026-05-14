package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.MatchCaseTextFieldSkin;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.Skin;

/**
 * 匹配大小写输入框
 *
 * @author oyzh
 * @since 2025/10/13
 */
public class MatchCaseTextField extends LimitTextField {

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public MatchCaseTextFieldSkin skin() {
        if (this.getSkin() == null) {
            this.setSkin(this.createDefaultSkin());
        }
        return (MatchCaseTextFieldSkin) this.getSkin();
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
        return new MatchCaseTextFieldSkin(this);
    }
}
