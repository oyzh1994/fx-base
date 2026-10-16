package cn.oyzh.fx.xserver.display;

import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.common.log.JulLog;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * The host's menu bar, through AWT's system tray: where a forwarded application's notification-area
 * icon is shown (C9).
 *
 * <p><b>No native code, which the backlog item expected to need.</b> The item's plan said "JavaFX/AWT
 * has no menu-bar API; this needs JNI or a native helper" - and that is true of JavaFX, but not of
 * AWT: {@code java.awt.SystemTray} has been implemented on macOS for years and puts its
 * {@code TrayIcon} in the menu bar next to the clock. Measured before any of this was written: an
 * icon added from a plain Java process appeared in the menu bar.
 *
 * <p>One icon is one {@code TrayIcon}, updated in place as the docked windows redraw - so the menu
 * bar does not flicker through remove/add on every frame. The image arrives from the server's tray
 * poll thread, and everything here is handed to AWT's event dispatch thread, because that is the only
 * thread the toolkit's own classes are safe to touch from.
 */
final class AwtTrayIcon {


    /** The one icon this backend owns, or null when the menu bar shows nothing. */
    private TrayIcon icon;
    private boolean unavailable;

    /** Where clicks go - the tray bridge, once it has one to forward them to. */
    private DisplayBackend.TrayClick listener;

    boolean available() {
        if (unavailable) {
            return false;
        }
        boolean supported = SystemTray.isSupported();
        unavailable = !supported;
        if (!supported) {
            JulLog.warn("this platform has no system tray: a forwarded application's tray icons cannot"
                    + " be shown");
        }
        return supported;
    }

    void setListener(DisplayBackend.TrayClick listener) {
        this.listener = listener;
    }

    void set(BufferedImage image, String tooltip) {
        if (!available()) {
            return;
        }
        EventQueue.invokeLater(() -> apply(image, tooltip));
    }

    private void apply(BufferedImage image, String tooltip) {
        JulLog.debug("the menu bar is being shown a {} icon", image == null ? "no"
                : image.getWidth() + "x" + image.getHeight());
        try {
            if (image == null) {
                if (icon != null) {
                    SystemTray.getSystemTray().remove(icon);
                    icon = null;
                }
                return;
            }
            if (icon == null) {
                // The tooltip is what the platform shows on hover; the click a mouse listener gets
                // is in the icon image's own coordinates, which is the space the tray's icons are
                // composed in.
                icon = new TrayIcon(image, tooltip == null ? "notification area" : tooltip);
                icon.setImageAutoSize(true);
                icon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent event) {
                        DisplayBackend.TrayClick target = listener;
                        if (target == null) {
                            return;
                        }
                        target.clicked(event.getX(), event.getY(), event.getButton(), 1);
                    }
                });
                SystemTray.getSystemTray().add(icon);
                JulLog.info("an icon is in the menu bar now");
            } else {
                icon.setImage(image);
                if (tooltip != null) {
                    icon.setToolTip(tooltip);
                }
            }
        } catch (AWTException e) {
            // The platform refused the icon: worth one line, and the client's own icon window is
            // still where the client drew it, so nothing is lost that was ever visible.
            JulLog.warn("could not put an icon in the menu bar", e);
        }
    }

}
