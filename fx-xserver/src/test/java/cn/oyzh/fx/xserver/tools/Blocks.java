package cn.oyzh.fx.xserver.tools;

/**
 * Prints a grid of difference counts per block between two XWD captures, so the worst areas can be
 * located before anything is magnified.
 *
 * <p>Usage: {@code Blocks A.xwd B.xwd [blockSize [x y w h]]}. Part of the verification kit
 * ({@code docs/backlog.md} §1); it is how "every icon in the toolbar differs" or "only the top fifty
 * rows differ" is read off a comparison in one line per row.
 */
public class Blocks {

    public static void main(String[] a) throws Exception {
        int[] p = Xwd.load(a[0]);
        int w = Xwd.width;
        int h = Xwd.height;
        int[] q = Xwd.load(a[1]);
        int size = a.length > 2 ? Integer.parseInt(a[2]) : 16;
        int x0 = a.length > 3 ? Integer.parseInt(a[3]) : 0;
        int y0 = a.length > 4 ? Integer.parseInt(a[4]) : 0;
        int rw = a.length > 5 ? Integer.parseInt(a[5]) : w;
        int rh = a.length > 6 ? Integer.parseInt(a[6]) : h;
        System.out.print("      ");
        for (int x = x0; x < x0 + rw; x += size) {
            System.out.printf("%5d", x);
        }
        System.out.println();
        for (int y = y0; y < y0 + rh; y += size) {
            System.out.printf("%5d ", y);
            for (int x = x0; x < x0 + rw; x += size) {
                int diff = 0;
                for (int by = y; by < Math.min(y + size, y0 + rh); by++) {
                    for (int bx = x; bx < Math.min(x + size, x0 + rw); bx++) {
                        if (p[by * w + bx] != q[by * w + bx]) {
                            diff++;
                        }
                    }
                }
                System.out.printf("%5s", diff == 0 ? "." : String.valueOf(diff));
            }
            System.out.println();
        }
    }
}
