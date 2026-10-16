package cn.oyzh.fx.xserver.tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Reports what a rectangle of a PNG contains - the colours in it and an ASCII map, darker pixels
 * first - so a region can be compared without looking at it.
 *
 * <p>Usage: {@code Rect file.png x y w h [columns]}. Part of the verification kit
 * ({@code docs/backlog.md} §1); a uniform block where a dialog used to be is how a "ghost" - a window
 * that closed but whose pixels were never repainted - is spotted without a screenshot viewer.
 */
public class Rect {

    public static void main(String[] args) throws Exception {
        BufferedImage image = ImageIO.read(new File(args[0]));
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        int w = Integer.parseInt(args[3]);
        int h = Integer.parseInt(args[4]);
        int columns = args.length > 5 ? Integer.parseInt(args[5]) : 60;
        Map<Integer, Integer> counts = new HashMap<>();
        int dark = 0;
        int min = 255;
        int max = 0;
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int rgb = image.getRGB(x + col, y + row) & 0xFFFFFF;
                counts.merge(rgb, 1, Integer::sum);
                int luma = ((rgb >> 16 & 0xFF) + (rgb >> 8 & 0xFF) + (rgb & 0xFF)) / 3;
                min = Math.min(min, luma);
                max = Math.max(max, luma);
                if (luma < 128) {
                    dark++;
                }
            }
        }
        System.out.printf("rect %d,%d %dx%d: %d colours, %d dark pixels, luma %d..%d%n",
                x, y, w, h, counts.size(), dark, min, max);
        counts.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(6)
                .forEach(e -> System.out.printf("   #%06X x%d%n", e.getKey(), e.getValue()));
        int rows = Math.max(1, columns * h / w / 2);
        int step = Math.max(1, w / columns);
        StringBuilder line = new StringBuilder();
        for (int row = 0; row < h; row += step * 2) {
            line.setLength(0);
            for (int col = 0; col < w; col += step) {
                int rgb = image.getRGB(x + col, y + row) & 0xFFFFFF;
                int luma = ((rgb >> 16 & 0xFF) + (rgb >> 8 & 0xFF) + (rgb & 0xFF)) / 3;
                line.append(luma < 100 ? '#' : luma < 190 ? '+' : luma < 240 ? '-' : '.');
            }
            System.out.println("   " + line);
            if (--rows <= 0) {
                break;
            }
        }
    }
}
