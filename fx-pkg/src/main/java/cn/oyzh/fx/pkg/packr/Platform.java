package cn.oyzh.fx.pkg.packr;

public enum Platform {
    Windows64("windows64"),
    Linux64("linux64"),
    MacOS("mac"),
    WIN_AMD64("win_amd64"),
    MACOS_AMD64("macos_amd64"),
    MACOS_ARM64("macos_arm64"),
    LINUX_AMD64("linux_amd64"),
    LINUX_ARM64("linux_arm64");

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

    public boolean isMacos() {
        return this == MacOS || this == MACOS_AMD64 || this == MACOS_ARM64;
    }
}
