package cn.oyzh.fx.tty;

import com.jediterm.core.util.TermSize;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author oyzh
 * @since 2026-07-07
 */
public interface TtyTerminalSizeable {

    /**
     * 获取终端大小
     *
     * @return 结果
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
     * @return 结果
     */
    SimpleObjectProperty<TermSize> terminalSizeProperty();

}
