package cn.oyzh.fx.terminal;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class TerminalConst {

    public static final String SCAN_BASE = "terminal.scan.base";

    public static void scanBase(String scanBase) {
        System.setProperty(SCAN_BASE, scanBase);
    }

    public static String scanBase() {
        return System.getProperty(SCAN_BASE);
    }
}
