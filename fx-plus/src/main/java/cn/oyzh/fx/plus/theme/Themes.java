package cn.oyzh.fx.plus.theme;

import java.util.ArrayList;
import java.util.List;

/**
 * 主题列表
 *
 * @author oyzh
 * @since 2024/4/3
 */
public class Themes {

    public static final SystemTheme SYSTEM = new SystemTheme();

    public static final CustomTheme CUSTOM = new CustomTheme();

    public static final DraculaTheme DRACULA = new DraculaTheme();

    public static final NordDarkTheme NORD_DARK = new NordDarkTheme();

    public static final NordLightTheme NORD_LIGHT = new NordLightTheme();

    public static final PrimerDarkTheme PRIMER_DARK = new PrimerDarkTheme();

    public static final PrimerLightTheme PRIMER_LIGHT = new PrimerLightTheme();

    public static final CupertinoDarkTheme CUPERTINO_DARK = new CupertinoDarkTheme();

    public static final CupertinoLightTheme CUPERTINO_LIGHT = new CupertinoLightTheme();

    public static final IntelliJDarkTheme INTELLIJ_DARK = new IntelliJDarkTheme();

    public static final IntelliJLightTheme INTELLIJ_LIGHT = new IntelliJLightTheme();

    public static final VSCodeDarkTheme VSCODE_DARK = new VSCodeDarkTheme();

    public static final VSCodeLightTheme VSCODE_LIGHT = new VSCodeLightTheme();

    public static final CyberpunkDarkTheme CYBERPUNK_DARK = new CyberpunkDarkTheme();

    public static final CyberpunkLightTheme CYBERPUNK_LIGHT = new CyberpunkLightTheme();

    public static final LiquidGlassDarkTheme LIQUID_GLASS_DARK = new LiquidGlassDarkTheme();

    public static final LiquidGlassLightTheme LIQUID_GLASS_LIGHT = new LiquidGlassLightTheme();

    public static final AnimeWarmDarkTheme ANIME_WARM_DARK = new AnimeWarmDarkTheme();

    public static final AnimeWarmLightTheme ANIME_WARM_LIGHT = new AnimeWarmLightTheme();

    public static final BusinessDarkTheme BUSINESS_DARK = new BusinessDarkTheme();

    public static final BusinessLightTheme BUSINESS_LIGHT = new BusinessLightTheme();

    //public static final BlackOnWhiteTheme BLACK_ON_WHITE = new BlackOnWhiteTheme();
    //
    //public static final WthiteOnBlackTheme WHITE_ON_BLACK = new WthiteOnBlackTheme();
    //
    //public static final YellowOnBlackTheme YELLOW_ON_BLACK = new YellowOnBlackTheme();

    //public static String[] styles() {
    //    return new String[]{
    //            DRACULA.getUserAgentStylesheet(),
    //            NORD_DARK.getUserAgentStylesheet(),
    //            NORD_LIGHT.getUserAgentStylesheet(),
    //            PRIMER_DARK.getUserAgentStylesheet(),
    //            PRIMER_LIGHT.getUserAgentStylesheet(),
    //            CUPERTINO_DARK.getUserAgentStylesheet(),
    //            CUPERTINO_LIGHT.getUserAgentStylesheet(),
    //            BLACK_ON_WHITE.getUserAgentStylesheet(),
    //            WHITE_ON_BLACK.getUserAgentStylesheet(),
    //            YELLOW_ON_BLACK.getUserAgentStylesheet(),
    //    };
    //}

    /**
     * 获取主题
     *
     * @return 主题列表
     */
    public static List<ThemeStyle> themes() {
        List<ThemeStyle> themes = new ArrayList<>(12);
        themes.add(PRIMER_LIGHT);
        themes.add(PRIMER_DARK);
        themes.add(NORD_LIGHT);
        themes.add(NORD_DARK);
        themes.add(CUPERTINO_LIGHT);
        themes.add(CUPERTINO_DARK);
        themes.add(DRACULA);
        themes.add(INTELLIJ_LIGHT);
        themes.add(INTELLIJ_DARK);
        themes.add(VSCODE_LIGHT);
        themes.add(VSCODE_DARK);
        themes.add(CYBERPUNK_LIGHT);
        themes.add(CYBERPUNK_DARK);
        themes.add(LIQUID_GLASS_LIGHT);
        themes.add(LIQUID_GLASS_DARK);
        themes.add(ANIME_WARM_LIGHT);
        themes.add(ANIME_WARM_DARK);
        themes.add(BUSINESS_LIGHT);
        themes.add(BUSINESS_DARK);
        //themes.add(WHITE_ON_BLACK);
        //themes.add(BLACK_ON_WHITE);
        //themes.add(YELLOW_ON_BLACK);
        return themes;
    }

    /**
     * 获取全部主题
     *
     * @return 主题
     */
    public static List<ThemeStyle> allThemes() {
        List<ThemeStyle> themes = themes();
        themes.add(SYSTEM);
        return themes;
    }

    /**
     * 获取主题
     *
     * @param name 主题名称
     * @return 主题
     */
    public static ThemeStyle getTheme(String name) {
        if (name == null) {
            return PRIMER_LIGHT;
        }
        return switch (name.toUpperCase()) {
            case "PRIMER LIGHT", "PRIMER_LIGHT" -> PRIMER_LIGHT;
            case "PRIMER DARK", "PRIMER_DARK" -> PRIMER_DARK;
            case "NORD LIGHT", "NORD_LIGHT" -> NORD_LIGHT;
            case "NORD DARK", "NORD_DARK" -> NORD_DARK;
            case "CUPERTINO LIGHT", "CUPERTINO_LIGHT" -> CUPERTINO_LIGHT;
            case "CUPERTINO DARK", "CUPERTINO_DARK" -> CUPERTINO_DARK;
            case "DRACULA" -> DRACULA;
            case "INTELLIJ LIGHT", "INTELLIJ_LIGHT" -> INTELLIJ_LIGHT;
            case "INTELLIJ DARK", "INTELLIJ_DARK" -> INTELLIJ_DARK;
            case "VS CODE LIGHT", "VSCODE_LIGHT", "VS_CODE_LIGHT" -> VSCODE_LIGHT;
            case "VS CODE DARK", "VSCODE_DARK", "VS_CODE_DARK" -> VSCODE_DARK;
            case "CYBERPUNK LIGHT", "CYBERPUNK_LIGHT" -> CYBERPUNK_LIGHT;
            case "CYBERPUNK DARK", "CYBERPUNK_DARK" -> CYBERPUNK_DARK;
            case "LIQUID GLASS LIGHT", "LIQUID_GLASS_LIGHT" -> LIQUID_GLASS_LIGHT;
            case "LIQUID GLASS DARK", "LIQUID_GLASS_DARK" -> LIQUID_GLASS_DARK;
            case "ANIME WARM LIGHT", "ANIME_WARM_LIGHT" -> ANIME_WARM_LIGHT;
            case "ANIME WARM DARK", "ANIME_WARM_DARK" -> ANIME_WARM_DARK;
            case "BUSINESS LIGHT", "BUSINESS_LIGHT" -> BUSINESS_LIGHT;
            case "BUSINESS DARK", "BUSINESS_DARK" -> BUSINESS_DARK;
            //case "WHITE ON BLACK", "WHITE_ON_BLACK" -> WHITE_ON_BLACK;
            //case "BLACK ON WHITE", "BLACK_ON_WHITE" -> BLACK_ON_WHITE;
            //case "YELLOW ON BLACK", "YELLOW_ON_BLACK" -> YELLOW_ON_BLACK;
            case "SYSTEM" -> SYSTEM;
            default -> PRIMER_LIGHT;
        };
    }

}
