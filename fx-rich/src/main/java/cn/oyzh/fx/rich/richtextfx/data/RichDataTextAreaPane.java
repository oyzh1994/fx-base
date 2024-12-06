package cn.oyzh.fx.rich.richtextfx.data;

import cn.oyzh.fx.rich.richtextfx.control.RichTextAreaPane;
import javafx.beans.property.BooleanProperty;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public class RichDataTextAreaPane extends RichTextAreaPane<RichDataTextArea> {

    public RichDataTextAreaPane() {
        super(new RichDataTextArea());
    }

    public void showData(Object data) {
        this.getContent().showData(data);
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

    public void setSearchText(String searchText) {
        this.getContent().setSearchText(searchText);
    }

    public String setSearchText() {
        return this.getContent().getSearchText();
    }

    public BooleanProperty editableProperty() {
        return this.getContent().editableProperty();
    }
}
