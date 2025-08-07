package cn.oyzh.fx.editor;


/**
 * @author oyzh
 * @since 2025/07/30
 */
public abstract class JsonEditorPane extends EditorPane {

    @Override
    public void showData(Object rawData) {
        this.showData(rawData, EditorFormatType.JSON);
    }

}
