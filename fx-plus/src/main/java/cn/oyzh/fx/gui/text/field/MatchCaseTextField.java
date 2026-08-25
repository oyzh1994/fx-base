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
    public MatchCaseTextFieldSkin skin() {
        return (MatchCaseTextFieldSkin) super.skin();
    }

    @Override
    protected MatchCaseTextFieldSkin createDefaultSkin() {
        return new MatchCaseTextFieldSkin(this);
    }
}
