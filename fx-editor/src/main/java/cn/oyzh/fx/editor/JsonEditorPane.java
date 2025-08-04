package cn.oyzh.fx.editor;

import javafx.scene.text.Font;

/**
 * @author oyzh
 * @since 2025/07/30
 */
public abstract class JsonEditorPane extends EditorPane {

    public void showData(Object rawData) {
        this.showJsonData(rawData);
    }

}
