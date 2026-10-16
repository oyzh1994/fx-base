package cn.oyzh.fx.xserver.tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Crops a screenshot around a point and blows it up, so a small region can be looked at.
 *
 * <p>Usage: {@code Crop file.png centreX centreY size scale out.png} - a square of {@code size}
 * pixels around the point, magnified {@code scale} times. Part of the verification kit
 * ({@code docs/backlog.md} §1); a 40x40 icon at 12x is how "the icon is drawn twice, offset" was
 * finally seen.
 */
public class Crop {

    public static void main(String[] args) throws Exception {
        BufferedImage image = ImageIO.read(new File(args[0]));
        int cx = Integer.parseInt(args[1]);
        int cy = Integer.parseInt(args[2]);
        int size = Integer.parseInt(args[3]);
        int scale = Integer.parseInt(args[4]);
        int half = size / 2;
        int x = Math.max(0, Math.min(image.getWidth() - size, cx - half));
        int y = Math.max(0, Math.min(image.getHeight() - size, cy - half));
        BufferedImage crop = image.getSubimage(x, y, size, size);
        BufferedImage out = new BufferedImage(size * scale, size * scale, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < size * scale; row++) {
            for (int col = 0; col < size * scale; col++) {
                out.setRGB(col, row, crop.getRGB(col / scale, row / scale));
            }
        }
        ImageIO.write(out, "png", new File(args[5]));
        System.out.println("crop " + x + "," + y + " " + size + "x" + size + " -> " + args[5]);
        System.out.println("  centre pixel: " + Integer.toHexString(crop.getRGB(half, half)));
        System.out.println("  corner pixel: " + Integer.toHexString(crop.getRGB(2, 2)));
    }
}
