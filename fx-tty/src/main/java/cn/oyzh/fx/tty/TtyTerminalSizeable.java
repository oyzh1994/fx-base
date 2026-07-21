package cn.oyzh.fx.tty;

import com.jediterm.core.util.TermSize;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author oyzh
 * @since 2026-07-07
 */
public interface TtyTerminalSizeable {

    default TermSize getTermSize() {
        if (this.terminalSizeProperty() == null) {
            return null;
        }
        return this.terminalSizeProperty().get();
    }

    SimpleObjectProperty<TermSize> terminalSizeProperty();

}
