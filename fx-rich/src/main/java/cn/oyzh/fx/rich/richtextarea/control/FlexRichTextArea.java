package cn.oyzh.fx.rich.richtextarea.control;

import cn.oyzh.fx.plus.adapter.AreaAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.ResourceUtil;
import cn.oyzh.fx.rich.richtextarea.RichTextColor;
import com.gluonhq.richtextarea.DefaultParagraphGraphicFactory;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/16
 */
public class FlexRichTextArea extends RichTextArea implements ThemeAdapter, FlexAdapter, AreaAdapter, TipAdapter {

    {
        NodeManager.init(this);
    }

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
        Document document = new Document(text);
        this.getActionFactory().open(document).execute(new ActionEvent());
    }

    public String getText() {
        return this.getDocument().getText();
    }

    public void append(String text) {
        String oldText = this.getText();
        String newText = oldText + text;
        List<DecorationModel> decorationList = new ArrayList<>();
        DecorationModel model = DecorationModel.createDefaultDecorationModel(newText.length());
        decorationList.add(model);
        Document document = new Document(newText, decorationList, 0);
        this.getActionFactory().open(document).execute(new ActionEvent());
    }

    public void appendLine(String text) {
        this.append("\n" + text);
    }

    public void clear() {
        this.setText("");
    }

    /**
     * 设置样式
     *
     * @param color 富文本样式
     */
    public void setColor(RichTextColor color) {
        if (color != null) {
            if (color.colorType() == 1) {
                FXUtil.runWait(() -> this.setForegroundColor(color.start(), color.end(), color.color()));
            } else {
                FXUtil.runWait(() -> this.setForegroundColor(color.start(), color.end(), color.color()));
            }
        }
    }

    public void setForegroundColor(int from, int to, Color color) {
        if (from < 0) {
            from = 0;
        }
        if (to < from) {
            to = from;
        }
        if (to > this.getTextLength()) {
            to = this.getTextLength();
        }
        // Document document = this.getDocument();
        TextDecoration decoration = TextDecoration
                .builder()
                .presets()
                .foreground(ThemeUtil.getColorHex(color))
                .build();
        this.getActionFactory().selectAndDecorate(new Selection(from, to), decoration).execute(new ActionEvent());

        // TextDecoration decoration1 = TextDecoration
        //         .builder()
        //         .presets()
        //         .foreground("black")
        //         .build();
        // ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
        // DecorationModel model = new DecorationModel(from, to, decoration, paragraphDecoration);
        // int len = this.getTextLength();
        // // DecorationModel model1 = new DecorationModel(5, len - 5, decoration1, paragraphDecoration);
        // List<DecorationModel> models = new ArrayList<>(document.getDecorations());
        // models.add(model);
        // // models.add(model1);
        // String text = document.getText();
        //
        // // document.getDecorations().add(model);
        // Document newDocument = new Document(text, models, 0);
        // Document newDocument = new Document(text, document.getDecorations(), 0);

    }

    @Override
    public String getUserAgentStylesheet() {
        return ResourceUtil.getResource("/com/gluonhq/richtextarea/rich-text-area.css").toExternalForm();
    }
}
