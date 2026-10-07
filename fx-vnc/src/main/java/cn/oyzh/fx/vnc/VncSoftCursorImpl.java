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
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import com.glavsoft.drawing.SoftCursor;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * 软光标的 JavaFX 实现，根据光标像素数据生成用于叠加绘制的可写图像。
 *
 * @author oyzh
 * @since 2026-07-18
 */
public class VncSoftCursorImpl extends SoftCursor implements Destroyable {

    /** 光标图像 */
    private WritableImage cursorImage;

    /**
     * 构造软光标。
     *
     * @param hotX   热点横坐标
     * @param hotY   热点纵坐标
     * @param width  光标宽度
     * @param height 光标高度
     */
    public VncSoftCursorImpl(int hotX, int hotY, int width, int height) {
        super(hotX, hotY, width, height);
    }

    /**
     * 获取光标图像。
     *
     * @return 光标图像
     */
    public WritableImage getImage() {
        return cursorImage;
    }

    @Override
    protected void createNewCursorImage(int[] cursorPixels, int hotX, int hotY, int width, int height) {
        if (width <= 0 || height <= 0) {
            cursorImage = null;
            return;
        }
        cursorImage = new WritableImage(width, height);
        PixelWriter pw = cursorImage.getPixelWriter();
        pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbPreInstance(), cursorPixels, 0, width);
    }

    @Override
    public void destroy() {
        NodeDestroyUtil.destroyNode(this.cursorImage);
        NodeDestroyUtil.destroyObject(this);
    }
}
