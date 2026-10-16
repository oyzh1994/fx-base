package cn.oyzh.fx.xserver.display;

/**
 * The pointer sprite a display backend draws over the frame it presents.
 *
 * <p>The sprite is deliberately kept out of the framebuffer. Painting it in - and saving the pixels
 * underneath so it can be taken out again - breaks three things at once: a client that draws where
 * the sprite sits has its pixels covered, the saved copy goes stale as soon as a client draws over
 * it so the next pointer move puts back content that has already been replaced (a trail of patches
 * wherever the pointer has been), and {@code GetImage} reads the sprite back as if it were the
 * application's own pixels. Handing it to the backend instead is what a hardware cursor does, and
 * it leaves the framebuffer holding nothing but what the clients drew.
 *
 * @param x      left edge, already offset by the cursor's hotspot
 * @param y      top edge, already offset by the cursor's hotspot
 * @param hotX   the hotspot, which is the point of the image the pointer is at
 * @param hotY   the same vertically
 * @param pixels non-premultiplied ARGB, {@code width * height} of them, row major
 */
public record CursorSprite(int x, int y, int width, int height, int hotX, int hotY, int[] pixels) {
}
