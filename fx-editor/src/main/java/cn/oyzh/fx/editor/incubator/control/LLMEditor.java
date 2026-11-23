package cn.oyzh.fx.editor.incubator.control;

import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.editor.incubator.EditorFormatType;

/**
 * LLM 聊天编辑器。
 * <p>
 * 使用 LLM 专用样式提供者，避免把聊天/运维相关配色写进公共编辑器配置。
 *
 * @author oyzh
 * @since 2026-11-23
 */
public class LLMEditor extends Editor {

    @Override
    public void initNode() {
        super.styleProvider = new LLMStyleProvider();
        super.initNode();
        super.setFormatType(EditorFormatType.LLM_CHAT);
    }
}
