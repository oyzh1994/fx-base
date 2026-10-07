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
 * @since 2025-10-13
 */
public class MatchCaseTextFieldSkin extends ActionTextFieldSkin {

    /**
     * 匹配大小写属性
     */
    private BooleanProperty matchCaseProperty = new SimpleBooleanProperty();

    /**
     * 是否匹配大小写
     *
     * @return 是否匹配大小写
     */
    public boolean isMatchCase() {
        return this.matchCaseProperty.get();
    }

    /**
     * 设置是否匹配大小写
     *
     * @param matchCase 是否匹配大小写
     */
    public void setMatchCase(boolean matchCase) {
        this.matchCaseProperty.set(matchCase);
    }

    /**
     * 匹配大小写只读属性包装
     */
    private ReadOnlyBooleanWrapper matchCasePropertyWrapper;

    /**
     * 获取匹配大小写只读属性
     *
     * @return 匹配大小写只读属性
     */
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

    /**
     * 激活态背景
     */
    private Background activeBackground;

    /**
     * 获取激活态背景
     *
     * @return 激活态背景
     */
    private Background activeBackground() {
        if (this.activeBackground == null) {
            Insets insets = new Insets(-3, -3, -3, -3);
            CornerRadii radii = new CornerRadii(3);
            BackgroundFill fill = new BackgroundFill(Color.valueOf("#E1EAF8"), radii, insets);
            this.activeBackground = new Background(fill);
        }
        return this.activeBackground;
    }

    /**
     * 悬停态背景
     */
    private Background focusBackground;

    /**
     * 获取悬停态背景
     *
     * @return 悬停态背景
     */
    private Background focusBackground() {
        if (this.focusBackground == null) {
            Insets insets = new Insets(-3, -3, -3, -3);
            CornerRadii radii = new CornerRadii(3);
            BackgroundFill fill = new BackgroundFill(Color.valueOf("#EDF3FB"), radii, insets);
            this.focusBackground = new Background(fill);
        }
        return this.focusBackground;
    }

    /**
     * 构造匹配大小写文本字段皮肤对象。
     *
     * @param textField 文本框
     */
    public MatchCaseTextFieldSkin(TextField textField) {
        super(textField);
    }

    @Override
    protected SVGGlyph getButton() {
        if (super.button == null) {
            super.button = new MatchCaseSVGGlyph();
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
