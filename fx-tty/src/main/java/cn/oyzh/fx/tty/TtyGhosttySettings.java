package cn.oyzh.fx.tty;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.tty.ghostty.TerminalTheme;
import cn.oyzh.fx.tty.ghostty.TerminalView;
import cn.oyzh.fx.plus.theme.ThemeManager;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.awt.Toolkit;
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

    private static final Color DEFAULT_LINK_COLOR = Color.web("#3B8EEA");

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
            var previous = view.getTheme();
            var next = theme(provider);
            // 主窗口背景图启用时，终端默认背景被置为透明；主题切换后要保留透明，
            // 但使用新主题的 RGB，避免退回不透明背景遮住背景图。
            if (previous != null && previous.background().getOpacity() < 1
                    && next.background().getOpacity() >= 1) {
                var background = next.background();
                next = next.withBackground(new Color(
                        background.getRed(),
                        background.getGreen(),
                        background.getBlue(),
                        previous.background().getOpacity()));
            }
            view.setTheme(next);
        } catch (Exception ex) {
            JulLog.debug("apply ghostty theme failed", ex);
        }
        try {
            int blinkMs = provider.caretBlinkingMs();
            view.setCursorBlinking(blinkMs > 0);
            if (blinkMs > 0) {
                view.setCursorBlinkInterval(blinkMs);
            }
        } catch (Exception ex) {
            JulLog.debug("apply ghostty cursor blinking failed", ex);
        }
        try {
            view.setMacOptionAsAlt(provider.altSendsEscape());
        } catch (Exception ex) {
            JulLog.debug("apply ghostty alt setting failed", ex);
        }
        try {
            int maxLines = provider.getBufferMaxLinesCount();
            if (maxLines <= 0) {
                maxLines = 5000;
            }
            // Ghostty 的 scrollback 限制单位是字节；按每行 1KB 估算，至少保留配置的行数。
            view.setScrollbackMaxBytes((long) maxLines * 1024L);
        } catch (Exception ex) {
            JulLog.debug("apply ghostty scrollback setting failed", ex);
        }
        try {
            view.setMaxRefreshRate(provider.maxRefreshRate());
        } catch (Exception ex) {
            JulLog.debug("apply ghostty refresh rate failed", ex);
        }
        try {
            view.setCopyOnSelect(provider.copyOnSelect());
        } catch (Exception ex) {
            JulLog.debug("apply ghostty copy-on-select failed", ex);
        }
        try {
            view.setUseAntialiasing(provider.useAntialiasing());
        } catch (Exception ex) {
            JulLog.debug("apply ghostty antialiasing failed", ex);
        }
        try {
            view.setOnBell(() -> {
                if (provider.audibleBell()) {
                    Toolkit.getDefaultToolkit().beep();
                }
            });
        } catch (Exception ex) {
            JulLog.debug("apply ghostty bell failed", ex);
        }
        if (provider instanceof TtyTermSettingsProvider settingsProvider) {
            try {
                view.setDefaultCursorStyle(settingsProvider.getCursorStyle());
            } catch (Exception ex) {
                JulLog.debug("apply ghostty cursor style failed", ex);
            }
            try {
                view.setHyperlinksEnabled(settingsProvider.isParseHyperlink());
            } catch (Exception ex) {
                JulLog.debug("apply ghostty hyperlink setting failed", ex);
            }
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
        // 滚动条属于 JavaFX 控件，JediTerm 使用应用主题；Ghostty 自绘滚动条同样取应用主题色。
        Color scrollbarBase = ThemeManager.currentForegroundColor();
        Color scrollbar = scrollbarBase.deriveColor(0, 1, 1, 0.45);
        Color scrollbarActive = scrollbarBase.deriveColor(0, 1, 1, 0.7);

        var theme = new TerminalTheme(
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
        return theme.withLinkColor(linkColor(provider, palette, foreground));
    }

    private static Color linkColor(SettingsProvider provider, ColorPalette palette, Color foreground) {
        try {
            TextStyle hyperlink = provider.getHyperlinkColor();
            if (hyperlink != null) {
                Color color = resolve(hyperlink.getForeground(), palette, false, DEFAULT_LINK_COLOR);
                if (color != null && !color.equals(foreground)) {
                    return color;
                }
            }
        } catch (Exception ex) {
            JulLog.debug("resolve ghostty hyperlink color failed", ex);
        }
        return DEFAULT_LINK_COLOR;
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
