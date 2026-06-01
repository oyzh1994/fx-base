package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.MatchCaseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * 匹配大小写输入框皮肤
 *
 * @author oyzh
 * @since 2025/10/13
 */
public class MatchCaseTextFieldSkin extends ActionTextFieldSkin {

    private BooleanProperty matchCaseProperty = new SimpleBooleanProperty();

    public boolean isMatchCase() {
        return this.matchCaseProperty.get();
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCaseProperty.set(matchCase);
    }

    private ReadOnlyBooleanWrapper matchCasePropertyWrapper;

    public ReadOnlyBooleanProperty matchCasePropery() {
        if (this.matchCasePropertyWrapper == null) {
            this.matchCasePropertyWrapper = new ReadOnlyBooleanWrapper();
            this.matchCasePropertyWrapper.bind(this.matchCaseProperty);
        }
        return this.matchCasePropertyWrapper;
    }

    @Override
    protected void onButtonClick(MouseEvent e) {
        this.setMatchCase(!this.isMatchCase());
        if (this.isMatchCase()) {
            this.button.setBackground(this.activeBackground());
        } else {
            this.button.setBackground(this.focusBackground());
        }
    }

    @Override
    protected void onButtonExit(MouseEvent e) {
        if (!this.isMatchCase()) {
            this.button.setBackground(null);
        }
    }

    @Override
    protected void onButtonEnter(MouseEvent e) {
        if (!this.isMatchCase()) {
            this.button.setBackground(this.focusBackground());
        }
    }

    private Background activeBackground;

    private Background activeBackground() {
        if (this.activeBackground == null) {
            Insets insets = new Insets(-3, -3, -3, -3);
            CornerRadii radii = new CornerRadii(3);
            BackgroundFill fill = new BackgroundFill(Color.valueOf("#E1EAF8"), radii, insets);
            this.activeBackground = new Background(fill);
        }
        return this.activeBackground;
    }

    private Background focusBackground;

    private Background focusBackground() {
        if (this.focusBackground == null) {
            Insets insets = new Insets(-3, -3, -3, -3);
            CornerRadii radii = new CornerRadii(3);
            BackgroundFill fill = new BackgroundFill(Color.valueOf("#EDF3FB"), radii, insets);
            this.focusBackground = new Background(fill);
        }
        return this.focusBackground;
    }

    public MatchCaseTextFieldSkin(TextField textField) {
        super(textField);
        // super(textField, new MatchCaseSVGGlyph("13"));
    }

    @Override
    protected SVGGlyph getButton() {
        if (super.button == null) {
            super.button = new MatchCaseSVGGlyph("13,10.4");
            super.initButton(super.button);
        }
        return super.button;
    }

    @Override
    protected void updateButtonVisibility() {
        this.button.display();
    }

//    @Override
//    protected void setButtonSize(double size) {
//        super.button.setSize(size, size * 0.8);
//    }

    @Override
    public void dispose() {
        this.matchCaseProperty.unbind();
        this.matchCaseProperty = null;
        super.dispose();
    }
}
