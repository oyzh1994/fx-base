package cn.oyzh.fx.editor.incubator;

import jfx.incubator.scene.control.richtext.LineNumberDecorator;

import java.text.DecimalFormat;

/**
 * 编辑器行号装饰器
 *
 * @author oyzh
 * @since 2025-08-15
 */
public class EditorLineNumberDecorator extends LineNumberDecorator {

    /**
     * 构造编辑器行数量装饰器对象。
     */
    public EditorLineNumberDecorator(){
        super(new DecimalFormat("###0"));
    }
}
