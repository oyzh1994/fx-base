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

import com.glavsoft.drawing.SoftCursor;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * JavaFX implementation of SoftCursor.
 * Creates a WritableImage from cursor pixel data for overlay rendering.
 */
public class VncSoftCursorImpl extends SoftCursor {

    private WritableImage cursorImage;

    public VncSoftCursorImpl(int hotX, int hotY, int width, int height) {
        super(hotX, hotY, width, height);
    }

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
}
