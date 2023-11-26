package cn.oyzh.fx.plus.controls.rich;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.richtext.StyledTextArea;


/**
 * fx富控件工具类
 *
 * @author oyzh
 * @since 2023/11/20
 */
@Slf4j
@UtilityClass
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
}
