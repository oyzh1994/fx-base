package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.theme.custom.AnimeWarmDarkTheme;
import cn.oyzh.fx.plus.theme.custom.AnimeWarmLightTheme;
import cn.oyzh.fx.plus.theme.custom.BusinessDarkTheme;
import cn.oyzh.fx.plus.theme.custom.BusinessLightTheme;
import cn.oyzh.fx.plus.theme.original.CupertinoDarkTheme;
import cn.oyzh.fx.plus.theme.original.CupertinoLightTheme;
import cn.oyzh.fx.plus.theme.custom.CustomTheme;
import cn.oyzh.fx.plus.theme.custom.CyberpunkDarkTheme;
import cn.oyzh.fx.plus.theme.custom.CyberpunkLightTheme;
import cn.oyzh.fx.plus.theme.custom.IntelliJDarkTheme;
import cn.oyzh.fx.plus.theme.custom.IntelliJLightTheme;
import cn.oyzh.fx.plus.theme.custom.LiquidGlassDarkTheme;
import cn.oyzh.fx.plus.theme.custom.LiquidGlassLightTheme;
import cn.oyzh.fx.plus.theme.custom.SystemTheme;
import cn.oyzh.fx.plus.theme.custom.VSCodeDarkTheme;
import cn.oyzh.fx.plus.theme.custom.VSCodeLightTheme;
import cn.oyzh.fx.plus.theme.ext.ArmyDarkTheme;
import cn.oyzh.fx.plus.theme.ext.ArmyLightTheme;
import cn.oyzh.fx.plus.theme.ext.AutumnTheme;
import cn.oyzh.fx.plus.theme.ext.BlackyTheme;
import cn.oyzh.fx.plus.theme.ext.BlueDarkTheme;
import cn.oyzh.fx.plus.theme.ext.BlueLightTheme;
import cn.oyzh.fx.plus.theme.ext.BrownyTheme;
import cn.oyzh.fx.plus.theme.ext.FallDarkTheme;
import cn.oyzh.fx.plus.theme.ext.FallLightTheme;
import cn.oyzh.fx.plus.theme.ext.GithubDarkColorblindTheme;
import cn.oyzh.fx.plus.theme.ext.GithubDarkTritanopiaTheme;
import cn.oyzh.fx.plus.theme.ext.GithubLightColorblindTheme;
import cn.oyzh.fx.plus.theme.ext.GithubLightDefaultTheme;
import cn.oyzh.fx.plus.theme.ext.GithubLightTritanopiaTheme;
import cn.oyzh.fx.plus.theme.ext.GithubSoftDarkTheme;
import cn.oyzh.fx.plus.theme.ext.NavyDarkTheme;
import cn.oyzh.fx.plus.theme.ext.NavyLightTheme;
import cn.oyzh.fx.plus.theme.ext.NewsTheme;
import cn.oyzh.fx.plus.theme.ext.SpringDarkTheme;
import cn.oyzh.fx.plus.theme.ext.SpringLightTheme;
import cn.oyzh.fx.plus.theme.ext.SummerDarkTheme;
import cn.oyzh.fx.plus.theme.ext.SummerLightTheme;
import cn.oyzh.fx.plus.theme.ext.WinterDarkTheme;
import cn.oyzh.fx.plus.theme.ext.WinterLightTheme;
import cn.oyzh.fx.plus.theme.ext.YachtTheme;
import cn.oyzh.fx.plus.theme.original.DraculaTheme;
import cn.oyzh.fx.plus.theme.original.NordDarkTheme;
import cn.oyzh.fx.plus.theme.original.NordLightTheme;
import cn.oyzh.fx.plus.theme.original.PrimerDarkTheme;
import cn.oyzh.fx.plus.theme.original.PrimerLightTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * 主题列表
 *
 * @author oyzh
 * @since 2024/4/3
 */
public class Themes {

    /**
     * 系统主题
     */
    public static final SystemTheme SYSTEM = new SystemTheme();

    /**
     * 自定义主题
     */
    public static final CustomTheme CUSTOM = new CustomTheme();

    /**
     * 德古拉暗色主题
     */
    public static final DraculaTheme DRACULA = new DraculaTheme();

    /**
     * 北欧暗色主题
     */
    public static final NordDarkTheme NORD_DARK = new NordDarkTheme();

    /**
     * 北欧亮色主题
     */
    public static final NordLightTheme NORD_LIGHT = new NordLightTheme();

    /**
     * 基础暗色主题
     */
    public static final PrimerDarkTheme PRIMER_DARK = new PrimerDarkTheme();

    /**
     * 基础亮色主题
     */
    public static final PrimerLightTheme PRIMER_LIGHT = new PrimerLightTheme();

    /**
     * 库比蒂诺暗色主题
     */
    public static final CupertinoDarkTheme CUPERTINO_DARK = new CupertinoDarkTheme();

    /**
     * 库比蒂诺亮色主题
     */
    public static final CupertinoLightTheme CUPERTINO_LIGHT = new CupertinoLightTheme();

    /**
     * IntelliJ IDEA 暗色主题
     */
    public static final IntelliJDarkTheme INTELLIJ_DARK = new IntelliJDarkTheme();

    /**
     * IntelliJ IDEA 亮色主题
     */
    public static final IntelliJLightTheme INTELLIJ_LIGHT = new IntelliJLightTheme();

    /**
     * VS Code 暗色主题
     */
    public static final VSCodeDarkTheme VSCODE_DARK = new VSCodeDarkTheme();

    /**
     * VS Code 亮色主题
     */
    public static final VSCodeLightTheme VSCODE_LIGHT = new VSCodeLightTheme();

    /**
     * 赛博朋克暗色主题
     */
    public static final CyberpunkDarkTheme CYBERPUNK_DARK = new CyberpunkDarkTheme();

    /**
     * 赛博朋克亮色主题
     */
    public static final CyberpunkLightTheme CYBERPUNK_LIGHT = new CyberpunkLightTheme();

    /**
     * 液态玻璃暗色主题
     */
    public static final LiquidGlassDarkTheme LIQUID_GLASS_DARK = new LiquidGlassDarkTheme();

    /**
     * 液态玻璃亮色主题
     */
    public static final LiquidGlassLightTheme LIQUID_GLASS_LIGHT = new LiquidGlassLightTheme();

    /**
     * 动漫温暖暗色主题
     */
    public static final AnimeWarmDarkTheme ANIME_WARM_DARK = new AnimeWarmDarkTheme();

    /**
     * 动漫温暖亮色主题
     */
    public static final AnimeWarmLightTheme ANIME_WARM_LIGHT = new AnimeWarmLightTheme();

    /**
     * 商务暗色主题
     */
    public static final BusinessDarkTheme BUSINESS_DARK = new BusinessDarkTheme();

    /**
     * 军旅亮色主题
     */
    public static final ArmyLightTheme ARMY_LIGHT = new ArmyLightTheme();

    /**
     * 军旅暗色主题
     */
    public static final ArmyDarkTheme ARMY_DARK = new ArmyDarkTheme();

    /**
     * 蓝色亮色主题
     */
    public static final BlueLightTheme BLUE_LIGHT = new BlueLightTheme();

    /**
     * 蓝色暗色主题
     */
    public static final BlueDarkTheme BLUE_DARK = new BlueDarkTheme();

    /**
     * 秋季亮色主题
     */
    public static final FallLightTheme FALL_LIGHT = new FallLightTheme();

    /**
     * 秋季暗色主题
     */
    public static final FallDarkTheme FALL_DARK = new FallDarkTheme();

    /**
     * 海军蓝亮色主题
     */
    public static final NavyLightTheme NAVY_LIGHT = new NavyLightTheme();

    /**
     * 海军蓝暗色主题
     */
    public static final NavyDarkTheme NAVY_DARK = new NavyDarkTheme();

    /**
     * 春季亮色主题
     */
    public static final SpringLightTheme SPRING_LIGHT = new SpringLightTheme();

    /**
     * 春季暗色主题
     */
    public static final SpringDarkTheme SPRING_DARK = new SpringDarkTheme();

    /**
     * 夏季亮色主题
     */
    public static final SummerLightTheme SUMMER_LIGHT = new SummerLightTheme();

    /**
     * 夏季暗色主题
     */
    public static final SummerDarkTheme SUMMER_DARK = new SummerDarkTheme();

    /**
     * 冬季亮色主题
     */
    public static final WinterLightTheme WINTER_LIGHT = new WinterLightTheme();

    /**
     * 冬季暗色主题
     */
    public static final WinterDarkTheme WINTER_DARK = new WinterDarkTheme();

    /**
     * GitHub 默认亮色主题
     */
    public static final GithubLightDefaultTheme GITHUB_LIGHT_DEFAULT = new GithubLightDefaultTheme();

    /**
     * GitHub 柔和暗色主题
     */
    public static final GithubSoftDarkTheme GITHUB_SOFT_DARK = new GithubSoftDarkTheme();

    /**
     * GitHub 亮色色盲友好主题
     */
    public static final GithubLightColorblindTheme GITHUB_LIGHT_COLORBLIND = new GithubLightColorblindTheme();

    /**
     * GitHub 暗色色盲友好主题
     */
    public static final GithubDarkColorblindTheme GITHUB_DARK_COLORBLIND = new GithubDarkColorblindTheme();

    /**
     * GitHub 亮色蓝色盲友好主题
     */
    public static final GithubLightTritanopiaTheme GITHUB_LIGHT_TRITANOPIA = new GithubLightTritanopiaTheme();

    /**
     * GitHub 暗色蓝色盲友好主题
     */
    public static final GithubDarkTritanopiaTheme GITHUB_DARK_TRITANOPIA = new GithubDarkTritanopiaTheme();

    /**
     * Autumn 扩展主题
     */
    public static final AutumnTheme AUTUMN = new AutumnTheme();

    /**
     * Blacky 扩展主题
     */
    public static final BlackyTheme BLACKY = new BlackyTheme();

    /**
     * Browny 扩展主题
     */
    public static final BrownyTheme BROWNY = new BrownyTheme();

    /**
     * News 扩展主题
     */
    public static final NewsTheme NEWS = new NewsTheme();

    /**
     * Yacht 扩展主题
     */
    public static final YachtTheme YACHT = new YachtTheme();

    /**
     * 商务亮色主题
     */
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
     * 获取内置主题列表
     *
     * @return 内置主题列表
     */
    public static List<ThemeStyle> themes() {
        List<ThemeStyle> themes = new ArrayList<>(44);
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
        themes.add(ARMY_LIGHT);
        themes.add(ARMY_DARK);
        themes.add(BLUE_LIGHT);
        themes.add(BLUE_DARK);
        themes.add(FALL_LIGHT);
        themes.add(FALL_DARK);
        themes.add(NAVY_LIGHT);
        themes.add(NAVY_DARK);
        themes.add(SPRING_LIGHT);
        themes.add(SPRING_DARK);
        themes.add(SUMMER_LIGHT);
        themes.add(SUMMER_DARK);
        themes.add(WINTER_LIGHT);
        themes.add(WINTER_DARK);
        themes.add(GITHUB_LIGHT_DEFAULT);
        themes.add(GITHUB_SOFT_DARK);
        themes.add(GITHUB_LIGHT_COLORBLIND);
        themes.add(GITHUB_DARK_COLORBLIND);
        themes.add(GITHUB_LIGHT_TRITANOPIA);
        themes.add(GITHUB_DARK_TRITANOPIA);
        themes.add(AUTUMN);
        themes.add(BLACKY);
        themes.add(BROWNY);
        themes.add(NEWS);
        themes.add(YACHT);
        //themes.add(WHITE_ON_BLACK);
        //themes.add(BLACK_ON_WHITE);
        //themes.add(YELLOW_ON_BLACK);
        return themes;
    }

    /**
     * 获取全部主题列表
     *
     * @return 全部主题列表（含系统主题）
     */
    public static List<ThemeStyle> allThemes() {
        List<ThemeStyle> themes = themes();
        themes.add(SYSTEM);
        return themes;
    }

    /**
     * 根据名称获取主题
     *
     * @param name 主题名称，支持空格或下划线分隔，不区分大小写
     * @return 匹配的主题风格，未匹配时返回基础亮色主题
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
            case "ARMY LIGHT", "ARMY_LIGHT" -> ARMY_LIGHT;
            case "ARMY DARK", "ARMY_DARK" -> ARMY_DARK;
            case "BLUE LIGHT", "BLUE_LIGHT" -> BLUE_LIGHT;
            case "BLUE DARK", "BLUE_DARK" -> BLUE_DARK;
            case "FALL LIGHT", "FALL_LIGHT" -> FALL_LIGHT;
            case "FALL DARK", "FALL_DARK" -> FALL_DARK;
            case "NAVY LIGHT", "NAVY_LIGHT" -> NAVY_LIGHT;
            case "NAVY DARK", "NAVY_DARK" -> NAVY_DARK;
            case "SPRING LIGHT", "SPRING_LIGHT" -> SPRING_LIGHT;
            case "SPRING DARK", "SPRING_DARK" -> SPRING_DARK;
            case "SUMMER LIGHT", "SUMMER_LIGHT" -> SUMMER_LIGHT;
            case "SUMMER DARK", "SUMMER_DARK" -> SUMMER_DARK;
            case "WINTER LIGHT", "WINTER_LIGHT" -> WINTER_LIGHT;
            case "WINTER DARK", "WINTER_DARK" -> WINTER_DARK;
            case "GITHUB LIGHT DEFAULT", "GITHUB_LIGHT_DEFAULT" -> GITHUB_LIGHT_DEFAULT;
            case "GITHUB SOFT DARK", "GITHUB_SOFT_DARK" -> GITHUB_SOFT_DARK;
            case "GITHUB LIGHT COLORBLIND", "GITHUB_LIGHT_COLORBLIND" -> GITHUB_LIGHT_COLORBLIND;
            case "GITHUB DARK COLORBLIND", "GITHUB_DARK_COLORBLIND" -> GITHUB_DARK_COLORBLIND;
            case "GITHUB LIGHT TRITANOPIA", "GITHUB_LIGHT_TRITANOPIA" -> GITHUB_LIGHT_TRITANOPIA;
            case "GITHUB DARK TRITANOPIA", "GITHUB_DARK_TRITANOPIA" -> GITHUB_DARK_TRITANOPIA;
            case "AUTUMN" -> AUTUMN;
            case "BLACKY" -> BLACKY;
            case "BROWNY" -> BROWNY;
            case "NEWS" -> NEWS;
            case "YACHT" -> YACHT;
            //case "WHITE ON BLACK", "WHITE_ON_BLACK" -> WHITE_ON_BLACK;
            //case "BLACK ON WHITE", "BLACK_ON_WHITE" -> BLACK_ON_WHITE;
            //case "YELLOW ON BLACK", "YELLOW_ON_BLACK" -> YELLOW_ON_BLACK;
            case "SYSTEM" -> SYSTEM;
            default -> PRIMER_LIGHT;
        };
    }

}
