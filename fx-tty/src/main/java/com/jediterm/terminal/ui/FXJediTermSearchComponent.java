package com.jediterm.terminal.ui;

import cn.oyzh.fx.tty.TtyKeyListener;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyListener;


public interface FXJediTermSearchComponent extends JediTermSearchComponent {

    @Override
    default JComponent getComponent() {
        return null;
    }

    @NotNull Pane getComponentFX();

    default void addKeyListener(@NotNull KeyListener listener) {

    }

    void addKeyListener(@NotNull TtyKeyListener listener);
}
