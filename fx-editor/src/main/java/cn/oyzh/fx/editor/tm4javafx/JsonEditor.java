package cn.oyzh.fx.editor.tm4javafx;

/**
 * @author oyzh
 * @since 2025/08/15
 */
public abstract class JsonEditor extends Editor {

    @Override
    public void showData(Object rawData) {
        this.showData(rawData, EditorFormatType.JSON);
    }

    @Override
    public void initNode() {
        super.initNode();
        super.setFormatType(EditorFormatType.JSON);
    }
}
