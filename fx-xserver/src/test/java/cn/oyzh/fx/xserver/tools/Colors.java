package cn.oyzh.fx.xserver.tools;

import java.util.TreeMap;

/**
 * Prints every distinct colour in a rectangle of an XWD capture, most common first.
 *
 * <p>Usage: {@code Colors in.xwd x y w h}. Part of the verification kit ({@code docs/backlog.md} §1):
 * it is how "the icon has three colours here and twenty against Xvfb" was read off, which is the
 * difference between antialiasing and coverage that has been quantised.
 */
public class Colors {

    public static void main(String[] a) throws Exception {
        int[] p = Xwd.load(a[0]);
        int w = Xwd.width;
        int x = Integer.parseInt(a[1]);
        int y = Integer.parseInt(a[2]);
        int rw = Integer.parseInt(a[3]);
        int rh = Integer.parseInt(a[4]);
        java.util.Map<Integer, Integer> counts = new TreeMap<>();
        for (int j = y; j < y + rh; j++) {
            for (int i = x; i < x + rw; i++) {
                counts.merge(p[j * w + i] & 0xFFFFFF, 1, Integer::sum);
            }
        }
        counts.entrySet().stream().sorted((u, v) -> v.getValue() - u.getValue())
                .forEach(e -> System.out.printf("#%06X x%d%n", e.getKey(), e.getValue()));
    }
}
