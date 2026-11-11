package cn.oyzh.fx.tty;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.tty.ghostty.TerminalTheme;
import cn.oyzh.fx.tty.ghostty.TerminalView;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * 把 JediTerm 的 {@link SettingsProvider} 映射为 Ghostty 的字体与主题。
 *
 * <p>两套终端共用 easyshell 的字体、背景色、前景色与 ANSI 调色板设置，切换引擎时
 * 视觉风格尽量保持一致。</p>
 *
 * @author oyzh
 * @since 2026-11-11
 */
final class TtyGhosttySettings {

    private static final Color[] DEFAULT_ANSI = {
            Color.rgb(0x00, 0x00, 0x00),
            Color.rgb(0xCD, 0x00, 0x00),
            Color.rgb(0x00, 0xCD, 0x00),
            Color.rgb(0xCD, 0xCD, 0x00),
            Color.rgb(0x00, 0x00, 0xEE),
            Color.rgb(0xCD, 0x00, 0xCD),
            Color.rgb(0x00, 0xCD, 0xCD),
            Color.rgb(0xE5, 0xE5, 0xE5),
            Color.rgb(0x7F, 0x7F, 0x7F),
            Color.rgb(0xFF, 0x00, 0x00),
            Color.rgb(0x00, 0xFF, 0x00),
            Color.rgb(0xFF, 0xFF, 0x00),
            Color.rgb(0x5C, 0x5C, 0xFF),
            Color.rgb(0xFF, 0x00, 0xFF),
            Color.rgb(0x00, 0xFF, 0xFF),
            Color.rgb(0xFF, 0xFF, 0xFF)
    };

    private TtyGhosttySettings() {
    }

    /**
     * 把设置应用到 Ghostty 视图。
     *
     * @param view     终端视图
     * @param provider 设置提供者
     */
    static void apply(TerminalView view, SettingsProvider provider) {
        if (view == null || provider == null) {
            return;
        }
        try {
            view.setFont(font(provider));
        } catch (Exception ex) {
            JulLog.debug("apply ghostty font failed", ex);
        }
        try {
            view.setTheme(theme(provider));
        } catch (Exception ex) {
            JulLog.debug("apply ghostty theme failed", ex);
        }
        try {
            view.setCursorBlinking(provider.caretBlinkingMs() > 0);
        } catch (Exception ex) {
            JulLog.debug("apply ghostty cursor blinking failed", ex);
        }
        try {
            view.setMacOptionAsAlt(provider.altSendsEscape());
        } catch (Exception ex) {
            JulLog.debug("apply ghostty alt setting failed", ex);
        }
    }

    static Font font(SettingsProvider provider) {
        if (provider instanceof FXDefaultSettingsProvider fxProvider) {
            Font font = fxProvider.getFXTerminalFont();
            if (font != null) {
                return font;
            }
        }
        float size = provider.getTerminalFontSize();
        return Font.font("Monospaced", size > 0 ? size : 14);
    }

    private static TerminalTheme theme(SettingsProvider provider) {
        ColorPalette palette = provider.getTerminalColorPalette();
        Color background = resolve(provider.getDefaultBackground(), palette, true, Color.BLACK);
        Color foreground = resolve(provider.getDefaultForeground(), palette, false, Color.WHITE);
        List<Color> colors = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            colors.add(paletteColor(palette, i, i < 8 ? Color.BLACK : Color.WHITE));
        }

        TextStyle selection = provider.getSelectionColor();
        Color selectionBackground = selection == null ? foreground.deriveColor(0, 1, 1, 0.3)
                : resolve(selection.getBackground(), palette, true, foreground.deriveColor(0, 1, 1, 0.3));
        Color selectionForeground = selection == null ? background
                : resolve(selection.getForeground(), palette, false, background);

        TextStyle found = provider.getFoundPatternColor();
        Color searchBackground = found == null ? foreground.deriveColor(0, 1, 1, 0.2)
                : resolve(found.getBackground(), palette, true, foreground.deriveColor(0, 1, 1, 0.2));
        Color searchBorder = found == null ? foreground
                : resolve(found.getForeground(), palette, false, foreground);
        Color currentSearchBackground = searchBackground.deriveColor(0, 1, 1, 0.45);
        Color scrollbar = foreground.deriveColor(0, 1, 1, 0.45);
        Color scrollbarActive = foreground.deriveColor(0, 1, 1, 0.7);

        return new TerminalTheme(
                background,
                foreground,
                colors,
                foreground,
                background,
                selectionBackground,
                selectionForeground,
                0.5,
                scrollbar,
                scrollbarActive,
                searchBackground,
                searchBorder,
                currentSearchBackground,
                searchBorder,
                0.45,
                0.45,
                0.85);
    }

    private static Color paletteColor(ColorPalette palette, int index, Color fallback) {
        if (palette == null) {
            return DEFAULT_ANSI[index];
        }
        try {
            Color color = toFx(palette.getForeground(TerminalColor.index(index)));
            return color != null ? color : DEFAULT_ANSI[index];
        } catch (Exception ex) {
            return DEFAULT_ANSI[index];
        }
    }

    private static Color resolve(TerminalColor color, ColorPalette palette, boolean background, Color fallback) {
        if (color == null) {
            return fallback;
        }
        try {
            Color converted;
            if (color.isIndexed() && palette != null) {
                converted = toFx(background ? palette.getBackground(color) : palette.getForeground(color));
            } else {
                converted = toFx(color.toColor());
            }
            return converted != null ? converted : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static Color toFx(com.jediterm.core.Color color) {
        if (color == null) {
            return null;
        }
        return Color.rgb(
                clamp(color.getRed()),
                clamp(color.getGreen()),
                clamp(color.getBlue()),
                color.getAlpha() / 255.0);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
