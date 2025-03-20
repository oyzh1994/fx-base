package cn.oyzh.fx.rich.richtextfx.data;

import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.rich.richtextfx.control.RichTextAreaPane;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public class RichDataTextAreaPane extends RichTextAreaPane<RichDataTextArea> implements NodeGroup {

    {
        this.addTextChangeListener((observable, oldValue, newValue) -> this.initTextStyle());
        this.init();
    }

    public RichDataTextAreaPane() {
        super(new RichDataTextArea());
    }

    public void showData(Object data) {
        this.getContent().showData(data);
    }

    public RichDataType showDetectData(Object rawData) {
        return this.getContent().showDetectData(rawData);
    }

    public void showJsonData(Object rawData) {
        this.showData(RichDataType.JSON, rawData);
    }

    public void showXmlData(Object rawData) {
        this.showData(RichDataType.XML, rawData);
    }

    public void showHtmlData(Object rawData) {
        this.showData(RichDataType.HTML, rawData);
    }

    public void showYamlData(Object rawData) {
        this.showData(RichDataType.YAML, rawData);
    }

    public void showBinaryData(Object rawData) {
        this.showData(RichDataType.BINARY, rawData);
    }

    public void showHexData(Object rawData) {
        this.showData(RichDataType.HEX, rawData);
    }

    public void showStringData(Object rawData) {
        this.showData(RichDataType.STRING, rawData);
    }

    public void showRawData(Object rawData) {
        this.showData(RichDataType.RAW, rawData);
    }

    public void showData(RichDataType dataType, Object rawData) {
        this.getContent().showData(dataType, rawData);
    }

    public void setDataType(RichDataType dataType) {
        this.getContent().setDataType(dataType);
    }

    public RichDataType getDataType() {
        return this.getContent().getDataType();
    }

    public void setHighlightText(String highlightText) {
        this.getContent().setHighlightText(highlightText);
    }

    public String getHighlightText() {
        return this.getContent().getHighlightText();
    }

    public BooleanProperty editableProperty() {
        return this.getContent().editableProperty();
    }

    public RichDataType getRealType() {
        return this.getContent().getRealType();
    }

    /**
     * 初始化组件
     */
    protected void init() {
        // 初始化字体
        this.initFont();
        // 显示行号
        this.showLineNum();
        // 覆盖默认的菜单
        this.setContextMenu(new ContextMenu());
    }

    /**
     * 初始化字体
     */
    protected void initFont() {
        // 禁用字体管理
        this.disableFont();
        this.setFontSize(11);
        this.setFontWeight(FontWeight.NORMAL);
    }

    @Override
    public void changeFont(Font font) {
        super.changeFont(font);
        this.initFont();
    }
}
