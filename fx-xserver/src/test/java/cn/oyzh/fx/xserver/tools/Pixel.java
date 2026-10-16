package cn.oyzh.fx.xserver.tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Prints the colour of one pixel of a PNG, so a screenshot can be checked without eyes.
 *
 * <p>Usage: {@code Pixel file.png x y}. Part of the verification kit ({@code docs/backlog.md} §1);
 * the images it reads are the ones the server dumps ({@code -Dfx-xserver.dumpFrame}) or the ones
 * {@link Xwd} converts from an {@code xwd} capture.
 */
public class Pixel {

    public static void main(String[] args) throws Exception {
        BufferedImage image = ImageIO.read(new File(args[0]));
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        System.out.printf("#%08X%n", image.getRGB(x, y));
    }
}
