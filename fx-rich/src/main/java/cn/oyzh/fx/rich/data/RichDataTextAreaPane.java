package cn.oyzh.fx.rich.data;

import cn.oyzh.fx.rich.control.RichTextAreaPane;

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
}
