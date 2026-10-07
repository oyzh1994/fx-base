package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.MatchCaseTextFieldSkin;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.Skin;

/**
 * 匹配大小写输入框
 *
 * @author oyzh
 * @since 2025-10-13
 */
public class MatchCaseTextField extends LimitTextField {

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

    @Override
    public MatchCaseTextFieldSkin skin() {
        return (MatchCaseTextFieldSkin) super.skin();
    }

    @Override
    protected MatchCaseTextFieldSkin createDefaultSkin() {
        return new MatchCaseTextFieldSkin(this);
    }
}
