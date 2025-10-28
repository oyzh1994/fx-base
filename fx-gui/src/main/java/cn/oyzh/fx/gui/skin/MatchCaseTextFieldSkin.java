package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.MatchCaseSVGGlyph;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

/**
 * 匹配大小写输入框皮肤
 *
 * @author oyzh
 * @since 2025/10/13
 */
public class MatchCaseTextFieldSkin extends ActionTextFieldSkin {

    public boolean isMatchCase() {
        return this.button.isActive();
    }

    public void setMatchCase(boolean matchCase) {
        this.button.setActive(matchCase);
    }

    public void addMatchCaseListener(Consumer<Boolean> listener) {
        this.button.activeProperty().addListener((observable, oldValue, newValue) -> {
            listener.accept(newValue);
        });
    }

    @Override
    protected void onButtonClicked(MouseEvent e) {
        this.setMatchCase(!this.isMatchCase());
    }

    public MatchCaseTextFieldSkin(TextField textField) {
        super(textField, new MatchCaseSVGGlyph("13"));
    }

    @Override
    protected void updateButtonVisibility() {
        this.button.display();
    }
}
