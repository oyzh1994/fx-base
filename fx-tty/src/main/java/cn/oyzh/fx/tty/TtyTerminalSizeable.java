package cn.oyzh.fx.tty;

import com.jediterm.core.util.TermSize;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 可获取终端尺寸的接口。
 *
 * @author oyzh
 * @since 2026-07-07
 */
public interface TtyTerminalSizeable {

    /**
     * 获取终端大小
     *
     * @return 终端大小
     */
    default TermSize getTermSize() {
        if (this.terminalSizeProperty() == null) {
            return null;
        }
        return this.terminalSizeProperty().get();
    }

    /**
     * 获取终端大小属性
     *
     * @return 终端大小属性
     */
    SimpleObjectProperty<TermSize> terminalSizeProperty();

}
