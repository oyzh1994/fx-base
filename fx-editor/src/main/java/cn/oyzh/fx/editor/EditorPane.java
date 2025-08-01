package cn.oyzh.fx.editor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * 编辑器面板
 *
 * @author oyzh
 * @since 2025/07/30
 */
public class EditorPane extends BaseEditorPane {

    public void showData(Object data, EditorFormatType formatType) {
        this.getEditor().showData(data, formatType);
    }

    public EditorFormatType showDetectData(Object rawData) {
        return this.getEditor().showDetectData(rawData);
    }

    public void showJsonData(Object rawData) {
        this.getEditor().showJsonData(rawData);
    }

    public void showXmlData(Object rawData) {
        this.getEditor().showXmlData(rawData);
    }

    public void showHtmlData(Object rawData) {
        this.getEditor().showHtmlData(rawData);
    }

    public void showYamlData(Object rawData) {
        this.getEditor().showYamlData(rawData);
    }

    public void showCssData(Object rawData) {
        this.getEditor().showCssData(rawData);
    }

    public void showPropertiesData(Object rawData) {
        this.getEditor().showPropertiesData(rawData);
    }

    public void showRawData(Object rawData) {
        this.getEditor().showRawData(rawData);
    }

    public void setFormatType(EditorFormatType formatType) {
        this.getEditor().setFormatType(formatType);
    }

    // @Override
    // public void initNode() {
    //     super.initNode();
    //     this.getVbar().valueProperty().addListener((observableValue, number, t1) -> {
    //         this.initTextStyleDelay();
    //     });
    // }
}
