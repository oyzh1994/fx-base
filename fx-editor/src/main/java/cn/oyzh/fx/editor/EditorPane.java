package cn.oyzh.fx.editor;

import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichDataType;
import cn.oyzh.fx.rich.richtextfx.control.RichTextAreaPane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.net.Socket;

/**
 * @author oyzh
 * @since 2025/07/30
 */
public class EditorPane extends RichTextAreaPane<Editor> implements NodeGroup {

    public EditorPane() {
        super(new Editor());
    }

    public void showData(Object data, EditorFormatType formatType) {
        this.getContent().showData(data, formatType);
    }

    public EditorFormatType showDetectData(Object rawData) {
        return this.getContent().showDetectData(rawData);
    }

    public void showJsonData(Object rawData) {
        this.getContent().showJsonData(rawData);
    }

    public void showXmlData(Object rawData) {
        this.getContent().showXmlData(rawData);
    }

    public void showHtmlData(Object rawData) {
        this.getContent().showHtmlData(rawData);
    }

    public void showYamlData(Object rawData) {
        this.getContent().showYamlData(rawData);
    }

    public void showCssData(Object rawData) {
        this.getContent().showCssData(rawData);
    }

    public void showPropertiesData(Object rawData) {
        this.getContent().showPropertiesData(rawData);
    }

    public void showBinaryData(Object rawData) {
        this.getContent().showBinaryData(rawData);
    }

    public void showHexData(Object rawData) {
        this.getContent().showHexData(rawData);
    }

    public void showStringData(Object rawData) {
        this.getContent().showStringData(rawData);
    }

    public void showRawData(Object rawData) {
        this.getContent().showRawData(rawData);
    }

    public void setFormatType(EditorFormatType formatType) {
        this.getContent().setFormatType(formatType);
    }

    public EditorFormatType getFormatType() {
        return this.getContent().getFormatType();
    }

    public BooleanProperty editableProperty() {
        return this.getContent().editableProperty();
    }

    public boolean isEmpty() {
        return this.getContent().isEmpty();
    }

    public void setLineNumPolicy(EditorLineNumPolicy lineNumPolicy) {
        this.getContent().setLineNumPolicy(lineNumPolicy);
    }

    public StringProperty highlightTextProperty() {
        return this.getContent().highlightTextProperty();
    }

    public ObjectProperty<EditorFormatType> formatTypeProperty() {
        return this.getContent().formatTypeProperty();
    }

    @Override
    public void initNode() {
        // 覆盖默认的菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        // vbar点击事件
        this.getVbar().addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            this.initTextStyle();
        });
        // vbar滚动事件
        this.getVbar().addEventFilter(ScrollEvent.SCROLL, event -> {
            this.initTextStyle();
        });
    }

}
