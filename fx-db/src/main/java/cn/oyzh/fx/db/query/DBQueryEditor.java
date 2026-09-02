package cn.oyzh.fx.db.query;

import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import javafx.scene.text.Font;

/**
 * 查询编辑器
 *
 * @author oyzh
 * @since 2025/01/21
 */
public abstract class DBQueryEditor extends Editor {

    /**
     * 获取提示弹窗
     *
     * @return 提示弹窗
     */
    protected abstract DBQueryPromptPopup<?, ?> promptPopup();

    @Override
    public void initNode() {
        this.setOnMouseReleased(e -> this.promptPopup().hide());
        this.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.promptPopup().hide();
            }
        });
        this.setOnKeyReleased(event -> {
            if (KeyboardUtil.isCtrlSlash(event)) {
                this.doComment();
                event.consume();
            } else {
                this.promptPopup().prompt(this, event);
            }
        });
        super.initNode();
    }

    /**
     * 执行注释
     */
    protected void doComment() {
    }

    @Override
    protected abstract Font getEditorFont();
}
