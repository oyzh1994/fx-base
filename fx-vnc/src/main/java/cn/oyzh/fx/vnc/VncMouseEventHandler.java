// Copyright (C) 2010 - 2014 GlavSoft LLC.
// All rights reserved.
//
// -----------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// -----------------------------------------------------------------------
//
package cn.oyzh.fx.vnc;

import com.glavsoft.rfb.IRepaintController;
import com.glavsoft.rfb.client.PointerEventMessage;
import com.glavsoft.rfb.protocol.Protocol;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * JavaFX replacement for MouseEventListener.java.
 * Translates JavaFX MouseEvent/ScrollEvent to RFB PointerEventMessage.
 */
public class VncMouseEventHandler {

    private static final byte BUTTON_LEFT = 1;
    private static final byte BUTTON_MIDDLE = 1 << 1;
    private static final byte BUTTON_RIGHT = 1 << 2;
    private static final byte WHEEL_UP = 1 << 3;
    private static final byte WHEEL_DOWN = 1 << 4;

    private final IRepaintController repaintController;
    private final Protocol protocol;
    private volatile double scaleFactor;

    public VncMouseEventHandler(IRepaintController repaintController, Protocol protocol, double scaleFactor) {
        this.repaintController = repaintController;
        this.protocol = protocol;
        this.scaleFactor = scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void handleMousePressed(MouseEvent event) {
        //event.getTarget();
        processMouseEvent(event, false);
    }

    public void handleMouseReleased(MouseEvent event) {
        processMouseEvent(event, false);
    }

    public void handleMouseDragged(MouseEvent event) {
        processMouseEvent(event, true);
    }

    public void handleMouseMoved(MouseEvent event) {
        processMouseEvent(event, true);
    }

    public void handleScroll(ScrollEvent event) {
        processScrollEvent(event);
    }

    private void processMouseEvent(MouseEvent event, boolean moved) {
        byte buttonMask = 0;
        short x = (short) (event.getX() / scaleFactor);
        short y = (short) (event.getY() / scaleFactor);

        if (moved) {
            repaintController.updateCursorPosition(x, y);
        }

        // Map JavaFX MouseButton to RFB button masks
        if (event.isPrimaryButtonDown()) {
            buttonMask |= BUTTON_LEFT;
        }
        if (event.isMiddleButtonDown()) {
            buttonMask |= BUTTON_MIDDLE;
        }
        if (event.isSecondaryButtonDown()) {
            buttonMask |= BUTTON_RIGHT;
        }

        protocol.sendMessage(new PointerEventMessage(buttonMask, x, y));
    }

    private void processScrollEvent(ScrollEvent event) {
        short x = (short) (event.getX() / scaleFactor);
        short y = (short) (event.getY() / scaleFactor);

        byte buttonMask = 0;

        double deltaY = event.getDeltaY();
        byte wheelMask = deltaY > 0 ? WHEEL_UP : WHEEL_DOWN;

        // Handle multiple notches (one event pair per notch)
        int notches = Math.abs((int) deltaY);
        // Clamp to a reasonable number to avoid flooding
        if (notches > 10) notches = 10;
        for (int i = 1; i < notches; ++i) {
            protocol.sendMessage(new PointerEventMessage((byte) (buttonMask | wheelMask), x, y));
            protocol.sendMessage(new PointerEventMessage(buttonMask, x, y));
        }
        protocol.sendMessage(new PointerEventMessage((byte) (buttonMask | wheelMask), x, y));

        event.consume();
    }
}
