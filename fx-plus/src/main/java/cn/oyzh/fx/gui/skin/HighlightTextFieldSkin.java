package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.WholeWordSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.MatchCaseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RegexSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.skin.FXTextFieldSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * 高亮文本输入框皮肤
 *
 * @author oyzh
 * @since 2026/05/14
 */
public class HighlightTextFieldSkin extends FXTextFieldSkin {

    public HighlightTextFieldSkin(TextField textField) {
        super(textField);
    }

    private RegexSVGGlyph regex;

    private WholeWordSVGGlyph wholeWord;

    private MatchCaseSVGGlyph matchCase;

    private final BooleanProperty regexProperty = new SimpleBooleanProperty();

    public boolean isRegex() {
        return this.regexProperty.get();
    }

    public void setRegex(boolean regex) {
        this.regexProperty.set(regex);
    }

    private ReadOnlyBooleanWrapper regexPropertyWrapper;

    public ReadOnlyBooleanProperty regexPropery() {
        if (this.regexPropertyWrapper == null) {
            this.regexPropertyWrapper = new ReadOnlyBooleanWrapper();
            this.regexPropertyWrapper.bind(this.regexProperty);
        }
        return regexPropertyWrapper;
    }

    private final BooleanProperty wholeWordProperty = new SimpleBooleanProperty();

    public boolean isWholeWord() {
        return this.wholeWordProperty.get();
    }

    public void setWholeWord(boolean wholeWord) {
        this.wholeWordProperty.set(wholeWord);
    }

    private ReadOnlyBooleanWrapper wholeWordPropertyWrapper;

    public ReadOnlyBooleanProperty wholeWordPropery() {
        if (this.wholeWordPropertyWrapper == null) {
            this.wholeWordPropertyWrapper = new ReadOnlyBooleanWrapper();
            this.wholeWordPropertyWrapper.bind(this.wholeWordProperty);
        }
        return this.wholeWordPropertyWrapper;
    }

    private final BooleanProperty matchCaseProperty = new SimpleBooleanProperty();

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

    private EventHandler<? super MouseEvent> regexMouseExitHandler;

    private EventHandler<? super MouseEvent> regexMouseEnterHandler;

    private EventHandler<? super MouseEvent> regexMouseClickHandler;

    private EventHandler<? super MouseEvent> wholeWordMouseExitHandler;

    private EventHandler<? super MouseEvent> wholeWordMouseEnterHandler;

    private EventHandler<? super MouseEvent> wholeWordMouseClickHandler;

    private EventHandler<? super MouseEvent> matchCaseMouseExitHandler;

    private EventHandler<? super MouseEvent> matchCaseMouseEnterHandler;

    private EventHandler<? super MouseEvent> matchCaseMouseClickHandler;

    private ChangeListener<? super Number> heightListener;

    private void doInit() {
        this.regexMouseExitHandler = event -> {
            if (!this.isRegex()) {
                this.regex.setBackground(null);
            }
        };
        this.regexMouseEnterHandler = event -> {
            if (!this.isRegex()) {
                this.regex.setBackground(this.focusBackground(this.regex));
            }
        };
        this.regexMouseClickHandler = event -> {
            this.setRegex(!this.isRegex());
            if (this.isRegex()) {
                this.regex.setBackground(this.activeBackground(this.regex));
            } else {
                this.regex.setBackground(this.focusBackground(this.regex));
            }
        };
        this.wholeWordMouseExitHandler = event -> {
            if (!this.isWholeWord()) {
                this.wholeWord.setBackground(null);
            }
        };
        this.wholeWordMouseEnterHandler = event -> {
            if (!this.isWholeWord()) {
                this.wholeWord.setBackground(this.focusBackground(this.regex));
            }
        };
        this.wholeWordMouseClickHandler = event -> {
            this.setWholeWord(!this.isWholeWord());
            if (this.isWholeWord()) {
                this.wholeWord.setBackground(this.activeBackground(this.wholeWord));
            } else {
                this.wholeWord.setBackground(this.focusBackground(this.regex));
            }
        };
        this.matchCaseMouseExitHandler = event -> {
            if (!this.isMatchCase()) {
                this.matchCase.setBackground(null);
            }
        };
        this.matchCaseMouseEnterHandler = event -> {
            if (!this.isMatchCase()) {
                this.matchCase.setBackground(this.focusBackground(this.regex));
            }
        };
        this.matchCaseMouseClickHandler = event -> {
            this.setMatchCase(!this.isMatchCase());
            if (this.isMatchCase()) {
                this.matchCase.setBackground(this.activeBackground(this.matchCase));
            } else {
                this.matchCase.setBackground(this.focusBackground(this.regex));
            }
        };
        this.heightListener = (observable, oldValue, newValue) -> {
            double val = newValue.doubleValue();
            Insets insets = this.getSkinnable().getPadding();
            if (insets != null) {
                val -= insets.getTop();
                val -= insets.getBottom();
            }
            double nHeight = NodeUtil.getHeight(this.regex);
            val -= (nHeight / 2);
            val /= 2;
            Insets insets1 = new Insets(val, 0, 0, 0);
            HBox.setMargin(this.matchCase, insets1);
            Insets insets2 = new Insets(val, 0, 0, 8);
            HBox.setMargin(this.regex, insets2);
            HBox.setMargin(this.wholeWord, insets2);
        };
    }

    @Override
    public ObjectProperty<Node> rightProperty() {
        if (super.rightProperty == null) {
            this.doInit();
            this.regex = new RegexSVGGlyph();
            this.regex.addEventFilter(MouseEvent.MOUSE_EXITED, this.regexMouseExitHandler);
            this.regex.addEventFilter(MouseEvent.MOUSE_ENTERED, this.regexMouseEnterHandler);
            this.regex.addEventFilter(MouseEvent.MOUSE_CLICKED, this.regexMouseClickHandler);
            this.wholeWord = new WholeWordSVGGlyph();
            this.wholeWord.addEventFilter(MouseEvent.MOUSE_EXITED, this.wholeWordMouseExitHandler);
            this.wholeWord.addEventFilter(MouseEvent.MOUSE_ENTERED, this.wholeWordMouseEnterHandler);
            this.wholeWord.addEventFilter(MouseEvent.MOUSE_CLICKED, this.wholeWordMouseClickHandler);
            this.matchCase = new MatchCaseSVGGlyph();
            this.matchCase.addEventFilter(MouseEvent.MOUSE_EXITED, this.matchCaseMouseExitHandler);
            this.matchCase.addEventFilter(MouseEvent.MOUSE_ENTERED, this.matchCaseMouseEnterHandler);
            this.matchCase.addEventFilter(MouseEvent.MOUSE_CLICKED, this.matchCaseMouseClickHandler);

            FXHBox hBox = new FXHBox();
            hBox.addChild(this.matchCase);
            hBox.addChild(this.wholeWord);
            hBox.addChild(this.regex);
            hBox.setPadding(Insets.EMPTY);

            this.getSkinnable().heightProperty().addListener(this.heightListener);
            super.rightProperty().set(hBox);
        }
        return super.rightProperty();
    }


    private Background activeBackground(SVGGlyph glyph) {
        // 控制背景色高度
        double b = this.regex.getRealHeight() - glyph.getRealHeight();
        Insets insets = new Insets(-3, -3, -3 - b, -3);
        CornerRadii radii = new CornerRadii(3);
        BackgroundFill fill = new BackgroundFill(Color.valueOf("#E1EAF8"), radii, insets);
        return new Background(fill);
    }

    private Background focusBackground(SVGGlyph glyph) {
        // 控制背景色高度
        double b = this.regex.getRealHeight() - glyph.getRealHeight();
        Insets insets = new Insets(-3, -3, -3 - b, -3);
        CornerRadii radii = new CornerRadii(3);
        BackgroundFill fill = new BackgroundFill(Color.valueOf("#EDFCCC"), radii, insets);
        return new Background(fill);
    }

    @Override
    public void dispose() {
        this.regex.removeEventFilter(MouseEvent.MOUSE_EXITED, this.regexMouseExitHandler);
        this.regex.removeEventFilter(MouseEvent.MOUSE_CLICKED, this.regexMouseClickHandler);
        this.regex.removeEventFilter(MouseEvent.MOUSE_ENTERED, this.regexMouseEnterHandler);
        this.wholeWord.removeEventFilter(MouseEvent.MOUSE_EXITED, this.wholeWordMouseExitHandler);
        this.wholeWord.removeEventFilter(MouseEvent.MOUSE_CLICKED, this.wholeWordMouseClickHandler);
        this.wholeWord.removeEventFilter(MouseEvent.MOUSE_ENTERED, this.wholeWordMouseEnterHandler);
        this.matchCase.removeEventFilter(MouseEvent.MOUSE_EXITED, this.matchCaseMouseExitHandler);
        this.matchCase.removeEventFilter(MouseEvent.MOUSE_CLICKED, this.matchCaseMouseClickHandler);
        this.matchCase.removeEventFilter(MouseEvent.MOUSE_ENTERED, this.matchCaseMouseEnterHandler);
        this.regexProperty.unbind();
        this.wholeWordProperty.unbind();
        this.matchCaseProperty.unbind();
        super.dispose();
    }
}
