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

import cn.oyzh.fx.plus.util.FXUtil;
import com.glavsoft.drawing.Renderer;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.transport.Transport;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.IntBuffer;

/**
 * JavaFX implementation of Renderer.
 * Uses PixelBuffer&lt;IntBuffer&gt; wrapping the same int[] pixels array for
 * zero-copy rendering to a JavaFX WritableImage.
 */
public class VncRendererImpl extends Renderer {

    private final WritableImage offscreenImage;
    private final PixelBuffer<IntBuffer> pixelBuffer;

    public VncRendererImpl(Transport transport, int width, int height, PixelFormat pixelFormat) {
        if (0 == width) width = 1;
        if (0 == height) height = 1;
        init(width, height, pixelFormat);

        IntBuffer intBuffer = IntBuffer.wrap(pixels);
        pixelBuffer = new PixelBuffer<>(width, height, intBuffer,
                javafx.scene.image.PixelFormat.getIntArgbPreInstance());
        offscreenImage = new WritableImage(pixelBuffer);

        cursor = new VncSoftCursorImpl(0, 0, 0, 0);
    }

    /**
     * Returns the WritableImage that backs this renderer.
     * The image is updated in-place via the PixelBuffer whenever pixels[] changes.
     */
    public WritableImage getOffscreenImage() {
        return offscreenImage;
    }

    /**
     * Mark a region of the pixel buffer as dirty so JavaFX re-renders it.
     */
    public void updateBuffer(FramebufferUpdateRectangle rect) {
        FXUtil.runLater(() -> pixelBuffer.updateBuffer(b -> null));
    }

    /**
     * Mark the entire pixel buffer as dirty.
     */
    public void updateBuffer() {
        FXUtil.runLater(() -> pixelBuffer.updateBuffer(b -> null));
    }

    /**
     * Draw JPEG image data into the framebuffer.
     * Uses ImageIO for synchronous JPEG decoding (does not require AWT event thread).
     */
    @Override
    public void drawJpegImage(byte[] bytes, int offset, int jpegBufferLength,
                              FramebufferUpdateRectangle rect) {
        try {
            BufferedImage jpegImage = ImageIO.read(
                    new ByteArrayInputStream(bytes, offset, jpegBufferLength));
            if (jpegImage == null) return;

            int imgWidth = jpegImage.getWidth();
            int imgHeight = jpegImage.getHeight();

            // Read ARGB pixels from the decoded JPEG
            int[] rgbArray = new int[imgWidth * imgHeight];
            jpegImage.getRGB(0, 0, imgWidth, imgHeight, rgbArray, 0, imgWidth);

            // Copy into the framebuffer pixels at the specified rectangle
            lock.lock();
            try {
                // Determine actual copy dimensions (min of JPEG size and rect size)
                int copyWidth = Math.min(imgWidth, rect.width);
                int copyHeight = Math.min(imgHeight, rect.height);

                for (int row = 0; row < copyHeight; row++) {
                    int srcPos = row * imgWidth;
                    int dstPos = (rect.y + row) * width + rect.x;
                    System.arraycopy(rgbArray, srcPos, pixels, dstPos, copyWidth);
                }
            } finally {
                lock.unlock();
            }
        } catch (IOException e) {
            // JPEG decode failed — the data may be corrupted
            // Ignore and let the next framebuffer update overwrite
        }
    }

    public VncSoftCursorImpl getCursor() {
        return (VncSoftCursorImpl) cursor;
    }
}
