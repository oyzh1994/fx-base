package cn.oyzh.fx.plus.theme;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * 主题列表
 *
 * @author oyzh
 * @since 2024/4/3
 */
@UtilityClass
public class Themes {

    public static final SystemTheme SYSTEM = new SystemTheme();

    public static final DraculaTheme DRACULA = new DraculaTheme();

    public static final NordDarkTheme NORD_DARK = new NordDarkTheme();

    public static final NordLightTheme NORD_LIGHT = new NordLightTheme();

    public static final PrimerDarkTheme PRIMER_DARK = new PrimerDarkTheme();

    public static final PrimerLightTheme PRIMER_LIGHT = new PrimerLightTheme();

    public static final CupertinoDarkTheme CUPERTINO_DARK = new CupertinoDarkTheme();

    public static final CupertinoLightTheme CUPERTINO_LIGHT = new CupertinoLightTheme();

    public static String[] styles() {
        return new String[]{
                DRACULA.getUserAgentStylesheet(),
                NORD_DARK.getUserAgentStylesheet(),
                NORD_LIGHT.getUserAgentStylesheet(),
                PRIMER_DARK.getUserAgentStylesheet(),
                PRIMER_LIGHT.getUserAgentStylesheet(),
                CUPERTINO_DARK.getUserAgentStylesheet(),
                CUPERTINO_LIGHT.getUserAgentStylesheet()
        };
    }

    public static List<ThemeStyle> themes() {
        List<ThemeStyle> themes = new ArrayList<>();
        themes.add(PRIMER_LIGHT);
        themes.add(PRIMER_DARK);
        themes.add(NORD_LIGHT);
        themes.add(NORD_DARK);
        themes.add(CUPERTINO_LIGHT);
        themes.add(CUPERTINO_DARK);
        themes.add(DRACULA);
        return themes;
    }

    public static List<ThemeStyle> allThemes() {
        List<ThemeStyle> themes = themes();
        themes.add(SYSTEM);
        return themes;
    }

    public static ThemeStyle getTheme(String name) {
        return switch (name.toUpperCase()) {
            case "PRIMER LIGHT", "PRIMER_LIGHT" -> PRIMER_LIGHT;
            case "PRIMER DARK", "PRIMER_DARK" -> PRIMER_DARK;
            case "NORD LIGHT", "NORD_LIGHT" -> NORD_LIGHT;
            case "NORD DARK", "NORD_DARK" -> NORD_DARK;
            case "CUPERTINO LIGHT", "CUPERTINO_LIGHT" -> CUPERTINO_LIGHT;
            case "CUPERTINO DARK", "CUPERTINO_DARK" -> CUPERTINO_DARK;
            case "DRACULA" -> DRACULA;
            case "SYSTEM" -> SYSTEM;
            default -> PRIMER_LIGHT;
        };
    }

}
