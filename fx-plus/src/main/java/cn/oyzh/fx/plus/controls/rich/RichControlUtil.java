package cn.oyzh.fx.plus.controls.rich;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Tab;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.richtext.StyledTextArea;

import java.util.Objects;


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
