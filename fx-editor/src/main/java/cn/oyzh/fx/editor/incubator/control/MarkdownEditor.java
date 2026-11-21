package cn.oyzh.fx.editor.incubator.control;

import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.editor.incubator.EditorFormatType;

/**
 * Markdown编辑器
 *
 * @author oyzh
 * @since 2026-06-11
 */
public class MarkdownEditor extends Editor {

    @Override
    public void initNode() {
        super.initNode();
        super.setFormatType(EditorFormatType.MARKDOWN);
    }
}
