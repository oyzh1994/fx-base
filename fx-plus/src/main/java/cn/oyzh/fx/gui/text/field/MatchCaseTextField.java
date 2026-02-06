package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.MatchCaseTextFieldSkin;
import javafx.scene.control.Skin;

import java.util.function.Consumer;

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
        MatchCaseTextFieldSkin skin = (MatchCaseTextFieldSkin) this.getSkin();
        if (skin == null) {
            this.setSkin(this.createDefaultSkin());
            skin = (MatchCaseTextFieldSkin) this.getSkin();
        }
        return skin;
    }


    public boolean isMatchCase() {
        return this.skin().isMatchCase();
    }

    public void setMatchCase(boolean matchCase) {
        this.skin().setMatchCase(matchCase);
    }

    public void addMatchCaseListener(Consumer<Boolean> listener) {
        this.skin().addMatchCaseListener(listener);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MatchCaseTextFieldSkin(this);
    }
}
