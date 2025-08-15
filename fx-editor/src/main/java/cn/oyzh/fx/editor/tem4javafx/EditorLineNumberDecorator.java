package cn.oyzh.fx.editor.tem4javafx;

import jfx.incubator.scene.control.richtext.LineNumberDecorator;

import java.text.DecimalFormat;

/**
 * @author oyzh
 * @since 2025-08-15
 */
public class EditorLineNumberDecorator extends LineNumberDecorator {

    public EditorLineNumberDecorator(){
        super(new DecimalFormat("###0"));
    }
}
