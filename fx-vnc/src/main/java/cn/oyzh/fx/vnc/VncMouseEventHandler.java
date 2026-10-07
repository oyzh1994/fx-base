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
 * VNC 鼠标事件处理器，将 JavaFX 的鼠标与滚轮事件转换为 RFB 指针事件消息。
 *
 * @author oyzh
 * @since 2026-07-18
 */
public class VncMouseEventHandler {

    /** RFB 指针事件左键掩码 */
    private static final byte BUTTON_LEFT = 1;

    /** RFB 指针事件中键掩码 */
    private static final byte BUTTON_MIDDLE = 1 << 1;

    /** RFB 指针事件右键掩码 */
    private static final byte BUTTON_RIGHT = 1 << 2;

    /** RFB 指针事件滚轮上滚掩码 */
    private static final byte WHEEL_UP = 1 << 3;

    /** RFB 指针事件滚轮下滚掩码 */
    private static final byte WHEEL_DOWN = 1 << 4;

    /** 重绘控制器，用于更新光标位置 */
    private final IRepaintController repaintController;

    /** VNC 协议对象 */
    private final Protocol protocol;

    /** 缩放比例 */
    private volatile double scaleFactor;

    /**
     * 构造鼠标事件处理器。
     *
     * @param repaintController 重绘控制器
     * @param protocol          VNC 协议对象
     * @param scaleFactor       缩放比例
     */
    public VncMouseEventHandler(IRepaintController repaintController, Protocol protocol, double scaleFactor) {
        this.repaintController = repaintController;
        this.protocol = protocol;
        this.scaleFactor = scaleFactor;
    }

    /**
     * 设置缩放比例。
     *
     * @param scaleFactor 缩放比例
     */
    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    /**
     * 处理鼠标按下事件。
     *
     * @param event 鼠标事件
     */
    public void handleMousePressed(MouseEvent event) {
        //event.getTarget();
        processMouseEvent(event, false);
    }

    /**
     * 处理鼠标释放事件。
     *
     * @param event 鼠标事件
     */
    public void handleMouseReleased(MouseEvent event) {
        processMouseEvent(event, false);
    }

    /**
     * 处理鼠标拖拽事件。
     *
     * @param event 鼠标事件
     */
    public void handleMouseDragged(MouseEvent event) {
        processMouseEvent(event, true);
    }

    /**
     * 处理鼠标移动事件。
     *
     * @param event 鼠标事件
     */
    public void handleMouseMoved(MouseEvent event) {
        processMouseEvent(event, true);
    }

    /**
     * 处理鼠标滚轮事件。
     *
     * @param event 滚轮事件
     */
    public void handleScroll(ScrollEvent event) {
        processScrollEvent(event);
    }

    /**
     * 将鼠标事件转换为 RFB 指针事件并发送给远端。
     *
     * @param event 鼠标事件
     * @param moved 是否同时更新远端光标位置
     */
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

    /**
     * 将滚轮滚动量换算为滚轮按键事件并发送给远端。
     *
     * @param event 滚轮事件
     */
    private void processScrollEvent(ScrollEvent event) {
        short x = (short) (event.getX() / scaleFactor);
        short y = (short) (event.getY() / scaleFactor);

        byte buttonMask = 0;

        double deltaY = event.getDeltaY();
        byte wheelMask = deltaY > 0 ? WHEEL_UP : WHEEL_DOWN;

        // 将 delta 换算为滚轮 notch 数（传统鼠标滚轮一个 notch ≈ 40）
        int notches = (int) Math.ceil(Math.abs(deltaY) / 40.0);
        // 限制最大 notch 数，防止触控板快速滚动时发送过多消息
        if (notches > 5){
            notches = 5;
        }
        for (int i = 1; i < notches; ++i) {
            protocol.sendMessage(new PointerEventMessage((byte) (buttonMask | wheelMask), x, y));
            protocol.sendMessage(new PointerEventMessage(buttonMask, x, y));
        }
        protocol.sendMessage(new PointerEventMessage((byte) (buttonMask | wheelMask), x, y));

        event.consume();
    }
}
