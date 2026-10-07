package cn.oyzh.fx.gui.label;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.Node;

/**
 * 系统标签，文字颜色跟随当前主题前景色
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class SystemLabel extends FXLabel {

    /**
     * 构造系统标签对象。
     */
    public SystemLabel() {
        super("");
    }

    /**
     * 构造系统标签对象。
     *
     * @param text 文本
     */
    public SystemLabel(String text) {
        super(text);
    }

    /**
     * 构造系统标签对象。
     *
     * @param text 文本
     * @param graphic 图形
     */
    public SystemLabel(String text, Node graphic) {
        super(text, graphic);
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setTextFill(ThemeManager.currentForegroundColor());
    }
}
