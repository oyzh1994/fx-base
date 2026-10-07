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
 * VNC 帧缓冲视图，负责渲染远端桌面画面并处理鼠标、键盘等输入事件。
 * 通过 WritableImage 支撑的图像控件显示帧缓冲，并叠加光标图层；
 * 使用 Pane 而非 StackPane，以避免自动居中与尺寸约束行为。
 *
 * @author oyzh
 * @since 2026-07-18
 */
public class VncFramebufferView extends FXPane implements IRepaintController, Destroyable {

    /** 帧缓冲宽度 */
    private int fbWidth;

    /** 帧缓冲高度 */
    private int fbHeight;

    /** 帧缓冲渲染器 */
    private volatile VncRendererImpl renderer;

    /** 远端光标 */
    private VncSoftCursorImpl cursor;

    /** 鼠标事件处理器 */
    private VncMouseEventHandler mouseEventHandler;

    /** 键盘事件处理器 */
    private VncKeyEventHandler keyEventHandler;

    /** 是否显示远端光标 */
    private boolean showCursor;

    /** 是否已启用用户输入 */
    private boolean isUserInputEnabled;

    /** VNC 协议对象 */
    private Protocol protocol;

    /** 视图缩放比例 */
    private double scaleFactor;

    /** 帧缓冲图像控件 */
    private FXImageView framebufferImageView;

    /** 光标图像控件 */
    private FXImageView cursorImageView;

    /**
     * 构造Vnc帧缓冲查看对象。
     */
    public VncFramebufferView() {
    }

    /**
     * 构造帧缓冲视图。
     *
     * @param protocol         VNC 协议对象
     * @param scaleFactor      视图缩放比例
     * @param mouseCursorShape 本地鼠标光标形状
     */
    public VncFramebufferView(Protocol protocol, double scaleFactor, LocalMouseCursorShape mouseCursorShape) {
        this.init(protocol, scaleFactor, mouseCursorShape);
    }

    /**
     * 初始化帧缓冲视图，创建图像控件并设置初始输入与光标状态。
     *
     * @param protocol         VNC 协议对象
     * @param scaleFactor      视图缩放比例
     * @param mouseCursorShape 本地鼠标光标形状
     */
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

    /**
     * 启用或禁用用户输入，按需注册或移除鼠标、键盘事件过滤器。
     *
     * @param enable        是否启用用户输入
     * @param convertToAscii 是否将按键转换为 ASCII
     */
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

    /**
     * 设置本地鼠标光标形状。
     *
     * @param cursorShape 光标形状
     */
    public void setLocalCursorShape(LocalMouseCursorShape cursorShape) {
        if (LocalMouseCursorShape.SYSTEM_DEFAULT == cursorShape) {
            setCursor(Cursor.DEFAULT);
        } else {
            setCursor(Cursor.NONE);
        }
    }

    /**
     * 根据帧缓冲尺寸与缩放比例更新图像控件及视图的尺寸。
     */
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

    /**
     * 获取缩放比例。
     *
     * @return 缩放比例
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * 获取帧缓冲宽度。
     *
     * @return 帧缓冲宽度
     */
    public int getFbWidth() {
        return fbWidth;
    }

    /**
     * 获取帧缓冲高度。
     *
     * @return 帧缓冲高度
     */
    public int getFbHeight() {
        return fbHeight;
    }

    /**
     * 获取协议。
     *
     * @return 协议
     */
    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public void destroy() {
        if (this.cursor != null) {
            this.cursor.destroy();
            this.renderer.destroy();
            this.cursorImageView.destroy();
            this.framebufferImageView.destroy();
        }
        NodeDestroyUtil.destroyObject(this);
    }
}
