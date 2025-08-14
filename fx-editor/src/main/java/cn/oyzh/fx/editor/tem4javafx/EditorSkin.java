package cn.oyzh.fx.editor.tem4javafx;

import cn.oyzh.common.util.ReflectUtil;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.skin.CodeAreaSkin;

/**
 * @author oyzh
 * @since 2025-08-14
 */
public class EditorSkin extends CodeAreaSkin {

    /**
     * Constructs the CodeArea skin.
     *
     * @param control the CodeArea instance
     */
    public EditorSkin(CodeArea control) {
        super(control);
    }

    public void setCartColor(Color color) {
        for (Node child : this.getChildren()) {
            if (child instanceof VFlow flow) {
                Path caretPath = ReflectUtil.getFieldValue(flow, "caretPath");
                caretPath.setStroke(color);
            }
        }
    }
}
