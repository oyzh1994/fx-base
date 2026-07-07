package cn.oyzh.fx.tty;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNull;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public abstract class TtyTtyConnector implements TtyConnector, TtyTerminalSizeable {

    @Override
    public void resize(@NotNull TermSize termSize) {
        this.terminalSizeProperty().set(termSize);
    }

    private SimpleObjectProperty<TermSize> terminalSizeProperty;

    public SimpleObjectProperty<TermSize> terminalSizeProperty() {
        if (this.terminalSizeProperty == null) {
            this.terminalSizeProperty = new SimpleObjectProperty<>();
        }
        return this.terminalSizeProperty;
    }
}