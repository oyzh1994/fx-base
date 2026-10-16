package cn.oyzh.fx.xserver.tools;

/**
 * Diff cost between two captures at every offset in ±4 pixels: says whether two renders disagree in
 * place or are the same picture moved.
 *
 * <p>Usage: {@code Shift A.xwd B.xwd [x y w h]}. Part of the verification kit
 * ({@code docs/backlog.md} §1); the minimum of the table is the answer - at (0,0) it is a rendering
 * difference, anywhere else it is a shift, and the two are very different bugs.
 */
public class Shift {

    public static void main(String[] a) throws Exception {
        int[] p = Xwd.load(a[0]);
        int w = Xwd.width;
        int h = Xwd.height;
        int[] q = Xwd.load(a[1]);
        int[] region = a.length > 5
                ? new int[]{Integer.parseInt(a[2]), Integer.parseInt(a[3]),
                        Integer.parseInt(a[4]), Integer.parseInt(a[5])}
                : new int[]{0, 0, w, h};
        for (int dy = -4; dy <= 4; dy++) {
            StringBuilder row = new StringBuilder();
            for (int dx = -4; dx <= 4; dx++) {
                int diff = 0;
                for (int y = region[1]; y < region[1] + region[3]; y++) {
                    for (int x = region[0]; x < region[0] + region[2]; x++) {
                        int px = x + dx;
                        int py = y + dy;
                        if (px < 0 || py < 0 || px >= w || py >= h || p[py * w + px] != q[y * w + x]) {
                            diff++;
                        }
                    }
                }
                row.append(String.format("%6d", diff));
            }
            System.out.println("dy=" + dy + row);
        }
    }
}
