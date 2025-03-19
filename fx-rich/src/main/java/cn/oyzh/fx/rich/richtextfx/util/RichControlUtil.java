package cn.oyzh.fx.rich.richtextfx.util;

import cn.oyzh.fx.rich.richtextfx.data.RichDataTextAreaPane;
import org.fxmisc.richtext.StyledTextArea;


/**
 * fx富控件工具类
 *
 * @author oyzh
 * @since 2023/11/20
 */

public class RichControlUtil {

    /**
     * 取消选中
     *
     * @param textArea 组件
     */
    public static void deselect(StyledTextArea<?, ?> textArea) {
        if (textArea != null) {
            textArea.deselect();
        }
    }

    /**
     * 取消选中
     *
     * @param pane 组件
     */
    public static void deselect(RichDataTextAreaPane pane) {
        if (pane != null) {
            pane.getContent().deselect();
        }
    }
}
