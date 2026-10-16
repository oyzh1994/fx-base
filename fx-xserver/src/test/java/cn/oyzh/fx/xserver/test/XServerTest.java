package cn.oyzh.fx.xserver.test;

import cn.oyzh.fx.xserver.Main;

/**
 *
 * @author oyzh
 * @since 2026-09-21
 */
public class XServerTest {

    public static void main() {
        String[] args_single = new String[]{"--display", "10", "--glx", "--single-window", "--geometry", "1300x800", "--log-requests"};
        String[] args_vnc = new String[]{"--display", "10", "--backend", "vnc", "--vnc-password", "123456", "--glx", "--geometry", "1300x800", "--log-requests"};
        String[] args_standard = new String[]{"--display", "10", "--glx", "--log-requests"};
        Main.main(args_standard);
    }
}
