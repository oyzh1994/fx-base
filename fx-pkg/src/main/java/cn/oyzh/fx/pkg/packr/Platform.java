package cn.oyzh.fx.pkg.packr;

public enum Platform {
    Windows64("windows64"),
    Linux64("linux64"),
    MacOS("mac"),
    Win_64("win_64");

    public final String desc;

    Platform(String desc) {
        this.desc = desc;
    }

    public static Platform byDesc(String desc) {
        for (Platform value : values()) {
            if (value.desc.equalsIgnoreCase(desc)) {
                return value;
            }
        }
        throw new RuntimeException("Invalid platform '" + desc + "'");
    }
}
