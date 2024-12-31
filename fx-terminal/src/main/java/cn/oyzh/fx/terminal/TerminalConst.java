package cn.oyzh.fx.terminal;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class TerminalConst {

    @Deprecated
    public static final String SCAN_BASE = "terminal.scan.base";

    @Deprecated
    public static void scanBase(String scanBase) {
        System.setProperty(SCAN_BASE, scanBase);
    }

    @Deprecated
    public static String scanBase() {
        return System.getProperty(SCAN_BASE);
    }

    @Deprecated
    public static String standard() {
        return "cn.oyzh.fx.terminal.standard";
    }
}
