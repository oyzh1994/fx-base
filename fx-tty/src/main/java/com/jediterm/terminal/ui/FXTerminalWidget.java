package com.jediterm.terminal.ui;

import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TtyConnector;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import javax.swing.*;
import java.awt.*;

/**
 * @author traff
 */
public interface FXTerminalWidget extends TerminalWidget {

    @Override
    default JediTermWidget createTerminalSession(TtyConnector ttyConnector){
        return null;
    }

    FXJediTermWidget createTerminalSessionFX(TtyConnector ttyConnector);

    @Override
   default JComponent getComponent(){
        return null;
    }

    Pane getComponentFX();

    default Pane getPreferredFocusableComponentFX() {
        return getComponentFX();
    }
}
