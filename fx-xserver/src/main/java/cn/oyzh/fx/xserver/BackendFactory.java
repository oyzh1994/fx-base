package cn.oyzh.fx.xserver;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.HeadlessBackend;
import cn.oyzh.fx.xserver.display.JavaFxBackend;
import cn.oyzh.fx.xserver.display.SwingBackend;
import cn.oyzh.fx.xserver.display.VncBackend;
import cn.oyzh.common.log.JulLog;

import java.lang.reflect.InvocationTargetException;

/**
 * Chooses the display backend the server runs on.
 *
 * <p>JavaFX is the primary backend, but it is also the one that can be missing at runtime: the
 * jars may not be on the classpath, the natives may not load, or there may be no display for the
 * toolkit. The server has to run on such machines anyway, so the JavaFX path is started for real
 * before it is chosen and any failure - {@link Throwable}, including the {@link LinkageError}
 * raised when the jars are absent - falls back to Swing instead of aborting the server.
 */
public final class BackendFactory {


    /** Title for the window when the caller has none; matches the default of {@link ServerOptions}. */
    public static final String DEFAULT_TITLE = "cn/oyzh/fx/xserver";

    private BackendFactory() {
    }

    /** Never returns null: an unusable JavaFX installation yields a Swing backend. */
    public static DisplayBackend create(ServerOptions options) {
        return switch (options.backend()) {
            case JAVAFX -> createJavaFx(options.title());
            case SWING -> createSwing(options.title());
            case HEADLESS -> new HeadlessBackend();
            // The only backend that needs an option of its own, and the only one whose "screen" is
            // not a window on this machine at all (backlog D2).
            case VNC -> new VncBackend(options.vncPort(), options.vncWebSocketPort(), options.title(),
                    !options.listenAll(), options.vncPassword());
        };
    }

    /**
     * The title the caller asked for, which is the name the window carries until the client publishes
     * one of its own - {@code --title} was parsed and documented but every backend was handed {@link
     * #DEFAULT_TITLE} instead, so the option did nothing at all.
     */
    private static DisplayBackend createJavaFx(String title) {
        try {
            // Starting the toolkit here is the only honest test of whether JavaFX works: a missing
            // class, a missing native library and a missing display all show up at this point.
            JavaFxBackend.verifyAvailable();
            return new JavaFxBackend(title);
        } catch (Throwable t) {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                return headlessInstead("JavaFX is not usable (" + describe(t) + ")");
            }
            JulLog.warn("JavaFX is not usable ({}), falling back to the swing backend", describe(t));
            JulLog.debug("JavaFX startup failed", t);
            return new SwingBackend(title);
        }
    }

    /** A Swing window, or the headless backend where there is no display to open one on. */
    private static DisplayBackend createSwing(String title) {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return headlessInstead("this machine has no display");
        }
        return new SwingBackend(title);
    }

    /**
     * The end of the fallback chain, said out loud.
     *
     * <p>A Swing window is opened on the event dispatch thread, so a machine that cannot show one
     * fails there rather than in {@code start} - out of the caller's sight. The server then served
     * every client, showed nothing, and logged nothing about it (C11). Where the platform itself says
     * there is no display there is no point trying the next toolkit: the honest answer is the headless
     * backend, and a line saying that is what happened.
     */
    private static DisplayBackend headlessInstead(String why) {
        JulLog.warn("{}: nothing can be shown on this machine, so the server is running headless -"
                + " clients are served and no window is opened", why);
        return new HeadlessBackend();
    }

    /** Names the real cause when the failure arrived wrapped in an initializer or invocation. */
    private static String describe(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause
                && (cause instanceof ExceptionInInitializerError
                || cause instanceof InvocationTargetException)) {
            cause = cause.getCause();
        }
        return cause.toString();
    }
}
