package cn.oyzh.fx.rich.richtextarea.control;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.util.ResourceUtil;
import cn.oyzh.fx.plus.adapter.AreaAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.ColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import cn.oyzh.fx.rich.richtextarea.ext.RichActionFactory;
import com.gluonhq.richtextarea.DefaultParagraphGraphicFactory;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.text.FontWeight;
import lombok.NonNull;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/16
 */
public class FlexRichTextArea extends RichTextArea implements ThemeAdapter, FlexAdapter, AreaAdapter, TipAdapter {

    {
        NodeManager.init(this);
//        ParagraphDecoration table = ParagraphDecoration.builder().presets().graphicType(ParagraphDecoration.GraphicType.NUMBERED_LIST)
//                .build();
//        DecorationModel model = new DecorationModel(0, 0, null, table);
//        Document document = new Document("", List.of(model), 0);
//        this.getActionFactory().open(document);
    }

    private final RichActionFactory actionFactory = new RichActionFactory(this);

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public String getFlexWidth() {
        return FlexAdapter.super.flexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super.flexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super.flexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super.flexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super.flexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super.flexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super.flexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super.flexY(flexY);
    }

    @Override
    public double getRealWidth() {
        return FlexAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super.realHeight(height);
    }

    @Override
    public void flushCaret() {

    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    @Override
    public void setFontSize(double fontSize) {
        AreaAdapter.super.fontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return AreaAdapter.super.fontSize();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
        AreaAdapter.super.fontFamily(fontFamily);
    }

    @Override
    public String getFontFamily() {
        return AreaAdapter.super.fontFamily();
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        AreaAdapter.super.fontWeight(fontWeight);
    }

    @Override
    public FontWeight getFontWeight() {
        return AreaAdapter.super.fontWeight();
    }

    @Override
    public void setStateManager(StateManager manager) {
        FlexAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return FlexAdapter.super.stateManager();
    }

    @Override
    public void initNode() {

    }

    public void setText(String text) {
        this.actionFactory.setText(text).execute(new ActionEvent());
        this.updateText();
    }

    public String getText() {
        RichTextAreaSkin skin = (RichTextAreaSkin) this.getSkin();
        if (skin != null) {
            RichTextAreaViewModel viewModel = skin.getViewModel();
            return viewModel.getTextBuffer().getText();
        }
        return this.getDocument().getText();
    }

    public void append(String text) {
        this.actionFactory.appendText(text).execute(new ActionEvent());
        this.updateText();
    }

    public void appendLine(String text) {
        this.append("\n" + text);
    }

    public void clear() {
        this.actionFactory.clearText().execute(new ActionEvent());
        this.updateText();
    }

    /**
     * 设置样式
     *
     * @param style 富文本样式
     */
    public void setStyle(RichTextStyle style) {
        if (style != null) {
            this.setStyle(style.start(), style.end(), style.style());
        }
    }

    /**
     * 设置样式
     *
     * @param styles 富文本样式
     */
    public void setStyles(List<RichTextStyle> styles) {
        if (CollectionUtil.isNotEmpty(styles)) {
            for (RichTextStyle style : styles) {
                this.setStyle(style.start(), style.end(), style.style());
            }
        }
    }

    public void setStyle(int from, int to, String color) {
        if (from < 0) {
            from = 0;
        }
        if (to < from) {
            to = from;
        }
        if (to > this.getTextLength()) {
            to = this.getTextLength();
        }
        color = ColorUtil.styleColorToWebColor(color);
        TextDecoration decoration = TextDecoration.builder().presets().foreground(color).build();
        this.actionFactory.setStyle(new Selection(from, to), decoration).execute(new ActionEvent());
    }

    public void clearTextStyle() {
        TextDecoration decoration = TextDecoration.builder().presets().build();
        this.actionFactory.setStyle(new Selection(0, this.getTextLength()), decoration).execute(new ActionEvent());
    }

    /**
     * 初始化文字样式
     */
    public void initTextStyle() {
    }

    @Override
    public String getUserAgentStylesheet() {
        return ResourceUtil.getResource("/com/gluonhq/richtextarea/rich-text-area.css").toExternalForm();
    }

    public void undo() {
        this.getActionFactory().undo().execute(new ActionEvent());
        this.updateText();
    }

    public void redo() {
        this.getActionFactory().redo().execute(new ActionEvent());
        this.updateText();
    }

    public void deleteText(int start, int end) {
        this.actionFactory.deleteText(start, end).execute(new ActionEvent());
        this.updateText();
    }

    public int getLength() {
        return Math.max(super.getTextLength(), 0);
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return this.getLength() <= 0;
    }

    public void positionCaret(int caretPosition) {
        this.actionFactory.positionCaret(caretPosition).execute(new ActionEvent());
        this.requestFocus();
    }

    public void insertText(String text) {
        this.actionFactory.insertText(text).execute(new ActionEvent());
        this.updateText();
    }

    /**
     * 忘记历史记录
     */
    public void forgetHistory() {
        this.actionFactory.forgetHistory().execute(new ActionEvent());
    }

    public void replaceSelection(String text) {
        this.actionFactory.replaceText(text).execute(new ActionEvent());
        this.updateText();
    }

    private StringProperty textProperty;

    public StringProperty textProperty() {
        if (this.textProperty == null) {
            this.textProperty = new SimpleStringProperty();
            this.textLengthProperty().addListener((observableValue, number, t1) -> this.updateText());
            this.modifiedProperty().addListener((observableValue, oldValue, newValue) -> this.updateText());
            this.updateText();
        }
        return this.textProperty;
    }

    @Override
    public void addTextChangeListener(ChangeListener<String> listener) {
        this.textProperty().addListener(listener);
    }

    private void updateText() {
        FXUtil.runPulse(() -> this.textProperty().set(this.getText()));
    }

    public void selectRange(int anchor, int caretPosition) {
        this.actionFactory.selectRange(anchor, caretPosition).execute(new ActionEvent());
    }

    /**
     * 设置是否显示行号
     *
     * @param showLine 是否显示行号
     */
    public void setShowLine(boolean showLine) {
        this.setProp("showLine", showLine);
        if (showLine) {
            this.showLineNum();
        } else {
            this.hideLineNum();
        }
    }

    /**
     * 获取设置是否显示行号
     */
    public boolean isShowLine() {
        return this.getProp("showLine");
    }

    /**
     * 显示行号
     */
    public void showLineNum() {
        if (this.getParagraphGraphicFactory() == DefaultParagraphGraphicFactory.getFactory()) {
            this.setParagraphGraphicFactory((integer, graphicType) -> new Label(integer.intValue() + ""));
        }
    }

    /**
     * 隐藏行号
     */
    public void hideLineNum() {
        this.setParagraphGraphicFactory(DefaultParagraphGraphicFactory.getFactory());
    }
}
