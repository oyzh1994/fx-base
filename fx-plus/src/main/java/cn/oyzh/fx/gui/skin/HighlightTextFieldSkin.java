package cn.oyzh.fx.gui.skin;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.svg.glyph.CloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.MatchCaseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RegexSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.WholeWordSVGGlyph;
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
 * @since 2026-05-14
 */
public class HighlightTextFieldSkin extends FXTextFieldSkin {

    /**
     * 构造高亮文本字段皮肤对象。
     *
     * @param textField 文本框
     */
    public HighlightTextFieldSkin(TextField textField) {
        super(textField);
    }

    /**
     * 清除按钮
     */
    private CloseSVGGlyph clear;

    /**
     * 正则匹配按钮
     */
    private RegexSVGGlyph regex;

    /**
     * 全词匹配按钮
     */
    private WholeWordSVGGlyph wholeWord;

    /**
     * 匹配大小写按钮
     */
    private MatchCaseSVGGlyph matchCase;

    /**
     * 正则匹配属性
     */
    private final BooleanProperty regexProperty = new SimpleBooleanProperty();

    /**
     * 是否正则匹配
     *
     * @return 是否正则匹配
     */
    public boolean isRegex() {
        return this.regexProperty.get();
    }

    /**
     * 设置是否正则匹配
     *
     * @param regex 是否正则匹配
     */
    public void setRegex(boolean regex) {
        this.regexProperty.set(regex);
    }

    /**
     * 正则匹配只读属性包装
     */
    private ReadOnlyBooleanWrapper regexPropertyWrapper;

    /**
     * 获取正则匹配只读属性
     *
     * @return 正则匹配只读属性
     */
    public ReadOnlyBooleanProperty regexPropery() {
        if (this.regexPropertyWrapper == null) {
            this.regexPropertyWrapper = new ReadOnlyBooleanWrapper();
            this.regexPropertyWrapper.bind(this.regexProperty);
        }
        return regexPropertyWrapper;
    }

    /**
     * 全词匹配属性
     */
    private final BooleanProperty wholeWordProperty = new SimpleBooleanProperty();

    /**
     * 是否全词匹配
     *
     * @return 是否全词匹配
     */
    public boolean isWholeWord() {
        return this.wholeWordProperty.get();
    }

    /**
     * 设置是否全词匹配
     *
     * @param wholeWord 是否全词匹配
     */
    public void setWholeWord(boolean wholeWord) {
        this.wholeWordProperty.set(wholeWord);
    }

    /**
     * 全词匹配只读属性包装
     */
    private ReadOnlyBooleanWrapper wholeWordPropertyWrapper;

    /**
     * 获取全词匹配只读属性
     *
     * @return 全词匹配只读属性
     */
    public ReadOnlyBooleanProperty wholeWordPropery() {
        if (this.wholeWordPropertyWrapper == null) {
            this.wholeWordPropertyWrapper = new ReadOnlyBooleanWrapper();
            this.wholeWordPropertyWrapper.bind(this.wholeWordProperty);
        }
        return this.wholeWordPropertyWrapper;
    }

    /**
     * 匹配大小写属性
     */
    private final BooleanProperty matchCaseProperty = new SimpleBooleanProperty();

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

    /**
     * 正则匹配按钮鼠标离开处理器
     */
    private EventHandler<? super MouseEvent> regexMouseExitHandler;

    /**
     * 正则匹配按钮鼠标进入处理器
     */
    private EventHandler<? super MouseEvent> regexMouseEnterHandler;

    /**
     * 正则匹配按钮鼠标点击处理器
     */
    private EventHandler<? super MouseEvent> regexMouseClickHandler;

    /**
     * 全词匹配按钮鼠标离开处理器
     */
    private EventHandler<? super MouseEvent> wholeWordMouseExitHandler;

    /**
     * 全词匹配按钮鼠标进入处理器
     */
    private EventHandler<? super MouseEvent> wholeWordMouseEnterHandler;

    /**
     * 全词匹配按钮鼠标点击处理器
     */
    private EventHandler<? super MouseEvent> wholeWordMouseClickHandler;

    /**
     * 匹配大小写按钮鼠标离开处理器
     */
    private EventHandler<? super MouseEvent> matchCaseMouseExitHandler;

    /**
     * 匹配大小写按钮鼠标进入处理器
     */
    private EventHandler<? super MouseEvent> matchCaseMouseEnterHandler;

    /**
     * 匹配大小写按钮鼠标点击处理器
     */
    private EventHandler<? super MouseEvent> matchCaseMouseClickHandler;

    /**
     * 控件高度变化监听器，用于调整按钮边距
     */
    private ChangeListener<? super Number> heightListener;

    /**
     * 更新清除按钮的显示状态
     */
    private void updateClearStatus() {
        if (this.getSkinnable().isFocused() && StringUtil.isNotEmpty(this.getText())) {
            this.clear.display();
        } else {
            this.clear.disappear();
        }
    }

    /**
     * 初始化监听器与按钮事件处理器
     */
    private void doInit() {
        this.getSkinnable().textProperty().addListener((observable, oldValue, newValue) -> {
            this.updateClearStatus();
        });
        this.getSkinnable().focusedProperty().addListener((observable, oldValue, newValue) -> {
            this.updateClearStatus();
        });
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
            HBox.setMargin(this.clear, insets1);
            Insets insets2 = new Insets(val, 0, 0, 8);
            HBox.setMargin(this.matchCase, insets2);
            HBox.setMargin(this.regex, insets2);
            HBox.setMargin(this.wholeWord, insets2);
        };
    }

    @Override
    public ObjectProperty<Node> rightProperty() {
        if (super.rightProperty == null) {
            this.doInit();
            this.clear = new CloseSVGGlyph();
            this.clear.setOnMousePrimaryClicked(event -> {
                this.getSkinnable().clear();
            });
            this.clear.disappear();
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
            hBox.addChild(this.clear);
            hBox.addChild(this.matchCase);
            hBox.addChild(this.wholeWord);
            hBox.addChild(this.regex);
            hBox.setPadding(Insets.EMPTY);

            this.getSkinnable().heightProperty().addListener(this.heightListener);
            super.rightProperty().set(hBox);
        }
        return super.rightProperty();
    }


    /**
     * 获取激活态背景
     *
     * @param glyph 目标图标
     * @return 激活态背景
     */
    private Background activeBackground(SVGGlyph glyph) {
        // 控制背景色高度
        double b = this.regex.getRealHeight() - glyph.getRealHeight();
        Insets insets = new Insets(-3, -3, -3 - b, -3);
        CornerRadii radii = new CornerRadii(3);
        BackgroundFill fill = new BackgroundFill(Color.valueOf("#E1EAF8"), radii, insets);
        return new Background(fill);
    }

    /**
     * 获取悬停态背景
     *
     * @param glyph 目标图标
     * @return 悬停态背景
     */
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
