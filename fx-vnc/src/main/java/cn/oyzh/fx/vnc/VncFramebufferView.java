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

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.controls.image.FXImageView;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.drawing.Renderer;
import com.glavsoft.rfb.IRepaintController;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.rfb.protocol.Protocol;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.transport.Transport;
import com.glavsoft.viewer.settings.LocalMouseCursorShape;
import com.glavsoft.viewer.settings.UiSettings;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * JavaFX replacement for Surface.java.
 * Displays the VNC framebuffer via a WritableImage-backed ImageView,
 * with a cursor overlay and input event handling.
 * Uses Pane (not StackPane) to avoid auto-centering/sizing behavior.
 */
public class VncFramebufferView extends FXPane implements IRepaintController, Destroyable {

    private int fbWidth;
    private int fbHeight;
    private volatile VncRendererImpl renderer;
    private VncSoftCursorImpl cursor;
    private VncMouseEventHandler mouseEventHandler;
    private VncKeyEventHandler keyEventHandler;
    private boolean showCursor;
    private boolean isUserInputEnabled;
    private Protocol protocol;
    private double scaleFactor;
    private FXImageView framebufferImageView;
    private FXImageView cursorImageView;

    public VncFramebufferView() {
    }

    public VncFramebufferView(Protocol protocol, double scaleFactor, LocalMouseCursorShape mouseCursorShape) {
        this.init(protocol, scaleFactor, mouseCursorShape);
    }

    public void init(Protocol protocol, double scaleFactor, LocalMouseCursorShape mouseCursorShape) {
        this.protocol = protocol;
        this.scaleFactor = scaleFactor;
        this.fbWidth = protocol.getFbWidth();
        this.fbHeight = protocol.getFbHeight();
        this.isUserInputEnabled = false;

        //        setStyle("-fx-background-color: black;");

        framebufferImageView = new FXImageView();
        framebufferImageView.setPreserveRatio(false);
        framebufferImageView.setSmooth(false);
        framebufferImageView.setCache(false);

        cursorImageView = new FXImageView();
        cursorImageView.setMouseTransparent(true);
        cursorImageView.setManaged(false);
        cursorImageView.setVisible(false);

        getChildren().add(framebufferImageView);
        getChildren().add(cursorImageView);

        setFocusTraversable(true);
        setOnMouseClicked(e -> requestFocus());

        if (!protocol.getSettings().isViewOnly()) {
            setUserInputEnabled(true, protocol.getSettings().isConvertToAscii());
        }
        showCursor = protocol.getSettings().isShowRemoteCursor();
        setLocalCursorShape(mouseCursorShape);

        updateImageViewSize();
    }

    public void setUserInputEnabled(boolean enable, boolean convertToAscii) {
        if (enable == isUserInputEnabled) {
            return;
        }
        isUserInputEnabled = enable;
        if (enable) {
            if (mouseEventHandler == null) {
                mouseEventHandler = new VncMouseEventHandler(this, protocol, scaleFactor);
            }
            this.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEventHandler::handleMousePressed);
            this.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEventHandler::handleMouseReleased);
            this.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEventHandler::handleMouseDragged);
            this.addEventFilter(MouseEvent.MOUSE_MOVED, mouseEventHandler::handleMouseMoved);
            this.addEventFilter(ScrollEvent.ANY, mouseEventHandler::handleScroll);
            if (keyEventHandler == null) {
                keyEventHandler = new VncKeyEventHandler(protocol);
            }
            keyEventHandler.setConvertToAscii(convertToAscii);
            this.addEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler::handleKeyPressed);
            this.addEventFilter(KeyEvent.KEY_RELEASED, keyEventHandler::handleKeyReleased);
            this.addEventFilter(KeyEvent.KEY_TYPED, keyEventHandler::handleKeyTyped);
        } else {
            if (mouseEventHandler != null) {
                this.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseEventHandler::handleMousePressed);
                this.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseEventHandler::handleMouseReleased);
                this.removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEventHandler::handleMouseDragged);
                this.removeEventFilter(MouseEvent.MOUSE_MOVED, mouseEventHandler::handleMouseMoved);
                this.removeEventFilter(ScrollEvent.ANY, mouseEventHandler::handleScroll);
            }
            if (keyEventHandler != null) {
                this.removeEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler::handleKeyPressed);
                this.removeEventFilter(KeyEvent.KEY_RELEASED, keyEventHandler::handleKeyReleased);
                this.removeEventFilter(KeyEvent.KEY_TYPED, keyEventHandler::handleKeyTyped);
            }
        }
    }

    @Override
    public Renderer createRenderer(Transport transport, int width, int height, PixelFormat pixelFormat) {
        renderer = new VncRendererImpl(transport, width, height, pixelFormat);
        cursor = renderer.getCursor();
        FXUtil.runLater(() -> {
            this.fbWidth = width;
            this.fbHeight = height;
            framebufferImageView.setImage(renderer.getOffscreenImage());
            updateImageViewSize();
            requestFocus();
        });
        return renderer;
    }

    @Override
    public void repaintBitmap(FramebufferUpdateRectangle rect) {
        if (renderer != null) {
            renderer.updateBuffer(rect);
        }
    }

    @Override
    public void repaintBitmap(int x, int y, int width, int height) {
        if (renderer != null) {
            renderer.updateBuffer();
        }
    }

    @Override
    public void repaintCursor() {
        if (cursor == null) {
            return;
        }
        FXUtil.runLater(() -> {
            synchronized (cursor.getLock()) {
                if (showCursor && cursor.getImage() != null) {
                    cursorImageView.setVisible(true);
                    cursorImageView.setImage(cursor.getImage());
                    cursorImageView.setLayoutX(cursor.rX * scaleFactor);
                    cursorImageView.setLayoutY(cursor.rY * scaleFactor);
                    cursorImageView.setFitWidth(cursor.width * scaleFactor);
                    cursorImageView.setFitHeight(cursor.height * scaleFactor);
                } else {
                    cursorImageView.setVisible(false);
                }
            }
        });
    }

    @Override
    public void updateCursorPosition(short x, short y) {
        FXUtil.runLater(() -> {
            synchronized (cursor.getLock()) {
                cursor.updatePosition(x, y);
                repaintCursor();
            }
        });
    }

    @Override
    public void settingsChanged(SettingsChangedEvent e) {
        if (ProtocolSettings.isRfbSettingsChangedFired(e)) {
            ProtocolSettings settings = (ProtocolSettings) e.getSource();
            FXUtil.runLater(() -> {
                setUserInputEnabled(!settings.isViewOnly(), settings.isConvertToAscii());
                showCursor = settings.isShowRemoteCursor();
            });
        } else if (UiSettings.isUiSettingsChangedFired(e)) {
            UiSettings uiSettings = (UiSettings) e.getSource();
            FXUtil.runLater(() -> {
                scaleFactor = uiSettings.getScaleFactor();
                if (uiSettings.isChangedMouseCursorShape()) {
                    setLocalCursorShape(uiSettings.getMouseCursorShape());
                }
                if (mouseEventHandler != null) {
                    mouseEventHandler.setScaleFactor(scaleFactor);
                }
                updateImageViewSize();
            });
        }
    }

    @Override
    public void setPixelFormat(PixelFormat pixelFormat) {
        if (renderer != null) {
            renderer.initColorDecoder(pixelFormat);
        }
    }

    public void setLocalCursorShape(LocalMouseCursorShape cursorShape) {
        if (LocalMouseCursorShape.SYSTEM_DEFAULT == cursorShape) {
            setCursor(Cursor.DEFAULT);
        } else {
            setCursor(Cursor.NONE);
        }
    }

    private void updateImageViewSize() {
        double scaledW = fbWidth * scaleFactor;
        double scaledH = fbHeight * scaleFactor;
        framebufferImageView.setFitWidth(scaledW);
        framebufferImageView.setFitHeight(scaledH);
        framebufferImageView.setLayoutX(0);
        framebufferImageView.setLayoutY(0);
        setMinSize(scaledW, scaledH);
        setPrefSize(scaledW, scaledH);
        setMaxSize(scaledW, scaledH);
        setMaxWidth(scaledW);
        setMaxHeight(scaledH);
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public int getFbWidth() {
        return fbWidth;
    }

    public int getFbHeight() {
        return fbHeight;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public void destroy() {
        this.cursor.destroy();
        this.renderer.destroy();
        this.cursorImageView.destroy();
        this.framebufferImageView.destroy();
        NodeDestroyUtil.destroyObject(this);
    }
}
