package cn.oyzh.fx.editor;

/**
 * @author oyzh
 * @since 2025/07/30
 */
public class JsonEditorPane extends BaseEditorPane {

    @Override
    public void showData(Object rawData) {
        this.getEditor().showJsonData(rawData);
    }
}
