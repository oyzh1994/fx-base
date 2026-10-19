package cn.oyzh.fx.plus.theme;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class ThemeUtilTest {

    @Test
    public void getsInverseThemeForAllPairedThemes() {
        assertInverse(Themes.PRIMER_LIGHT, Themes.PRIMER_DARK);
        assertInverse(Themes.NORD_LIGHT, Themes.NORD_DARK);
        assertInverse(Themes.CUPERTINO_LIGHT, Themes.CUPERTINO_DARK);
        assertInverse(Themes.INTELLIJ_LIGHT, Themes.INTELLIJ_DARK);
        assertInverse(Themes.VSCODE_LIGHT, Themes.VSCODE_DARK);
        assertInverse(Themes.CYBERPUNK_LIGHT, Themes.CYBERPUNK_DARK);
        assertInverse(Themes.LIQUID_GLASS_LIGHT, Themes.LIQUID_GLASS_DARK);
        assertInverse(Themes.ANIME_WARM_LIGHT, Themes.ANIME_WARM_DARK);
        assertInverse(Themes.BUSINESS_LIGHT, Themes.BUSINESS_DARK);
        assertInverse(Themes.ARMY_LIGHT, Themes.ARMY_DARK);
        assertInverse(Themes.BLUE_LIGHT, Themes.BLUE_DARK);
        assertInverse(Themes.FALL_LIGHT, Themes.FALL_DARK);
        assertInverse(Themes.NAVY_LIGHT, Themes.NAVY_DARK);
        assertInverse(Themes.SPRING_LIGHT, Themes.SPRING_DARK);
        assertInverse(Themes.SUMMER_LIGHT, Themes.SUMMER_DARK);
        assertInverse(Themes.WINTER_LIGHT, Themes.WINTER_DARK);
        assertInverse(Themes.GITHUB_LIGHT_DEFAULT, Themes.GITHUB_SOFT_DARK);
        assertInverse(Themes.GITHUB_LIGHT_COLORBLIND, Themes.GITHUB_DARK_COLORBLIND);
        assertInverse(Themes.GITHUB_LIGHT_TRITANOPIA, Themes.GITHUB_DARK_TRITANOPIA);
    }

    @Test
    public void fallsBackToPrimerForUnpairedThemes() {
        assertSame(Themes.PRIMER_LIGHT, ThemeUtil.getInverseTheme(Themes.DRACULA));
        ThemeStyle expected = Themes.AUTUMN.isDarkMode() ? Themes.PRIMER_LIGHT : Themes.PRIMER_DARK;
        assertSame(expected, ThemeUtil.getInverseTheme(Themes.AUTUMN));
    }

    private static void assertInverse(ThemeStyle light, ThemeStyle dark) {
        assertSame(dark, ThemeUtil.getInverseTheme(light));
        assertSame(light, ThemeUtil.getInverseTheme(dark));
    }
}
