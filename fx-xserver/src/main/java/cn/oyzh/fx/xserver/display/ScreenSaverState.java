package cn.oyzh.fx.xserver.display;

/**
 * The display's idle state: what the screen saver is set to, and what DPMS is set to.
 *
 * <p>One per server rather than one per screen, and the reason is the host window: what would blank is
 * that window, and there is one of them. A screen of its own per client already makes a per-screen
 * reading of these settings meaningless - every client's screen describes the same physical display -
 * so the settings live where the display does.
 *
 * <p><b>The server reports the settings and does not act on them.</b> Blanking the display is the host
 * window's business, and on this platform the system's own screen saver is what does it; a client that
 * asks for a ten-minute timeout, or that suspends the saver while a video plays, is answered honestly
 * and otherwise left alone. What the state *is* matters to two kinds of client: one that reads it back
 * (the numbers have to be the ones it set, or it cannot tell its own change took), and one that
 * subscribes to {@code ScreenSaverNotify} (it is told when the state changes, which here is when a
 * client forces it).
 */
public final class ScreenSaverState {

    /** What a client that never calls {@code SetScreenSaver} reads back: X's own default, 600 seconds. */
    public static final int DEFAULT_TIMEOUT_SECONDS = 600;

    /** The screen saver's states, numbered as MIT-SCREEN-SAVER numbers them. */
    public static final int OFF = 0;
    public static final int ON = 1;
    public static final int CYCLE = 2;
    public static final int DISABLED = 3;

    /** The core protocol's settings, which MIT-SCREEN-SAVER reports through {@code QueryInfo}. */
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private int intervalSeconds = DEFAULT_TIMEOUT_SECONDS;
    private boolean preferBlanking = true;
    private boolean allowExposures = true;

    private int state = OFF;
    /** Whether the last change was forced rather than the server's own doing. */
    private boolean forced;

    /** When the last input arrived, for the two countdowns {@code QueryInfo} reports. */
    private long lastInputMillis = System.currentTimeMillis();

    /** DPMS, which this server advertises and cannot act on: see {@code Capable}. */
    private int standbySeconds;
    private int suspendSeconds;
    private int offSeconds;
    private boolean dpmsEnabled;
    private int powerLevel;

    // ------------------------------------------------------------------
    // The screen saver
    // ------------------------------------------------------------------

    /**
     * Records input, which restarts the countdown a saver would use.
     *
     * <p>Called from the input path itself, so it is on the hot path for every motion, button and key -
     * one assignment, which is why it is not routed through anything cleverer.
     */
    public void noteInput() {
        lastInputMillis = System.currentTimeMillis();
    }

    public long millisSinceInput() {
        return Math.max(0, System.currentTimeMillis() - lastInputMillis);
    }

    /**
     * How long until a saver with this timeout would run - the number a client uses to decide whether
     * it needs to say "do not blank yet". Zero when the saver is disabled, which is what
     * {@code SetScreenSaver}'s {@code timeout = 0} means.
     */
    public long millisUntilSaver() {
        if (timeoutSeconds == 0) {
            return 0;
        }
        return Math.max(0, timeoutSeconds * 1000L - millisSinceInput());
    }

    public int timeoutSeconds() {
        return timeoutSeconds;
    }

    public int intervalSeconds() {
        return intervalSeconds;
    }

    public boolean preferBlanking() {
        return preferBlanking;
    }

    public boolean allowExposures() {
        return allowExposures;
    }

    /**
     * {@code SetScreenSaver}: the settings a client wants, kept so that {@code GetScreenSaver} and
     * {@code QueryInfo} answer with what was actually set rather than with defaults.
     *
     * <p>A timeout of zero disables the saver, which MIT-SCREEN-SAVER reports as a state of its own
     * rather than as "off with no timeout" - the difference is what tells a client whether asking is
     * worth it.
     */
    public void setSettings(int timeoutSeconds, int intervalSeconds, boolean preferBlanking,
                            boolean allowExposures) {
        this.timeoutSeconds = timeoutSeconds;
        this.intervalSeconds = intervalSeconds;
        this.preferBlanking = preferBlanking;
        this.allowExposures = allowExposures;
        if (timeoutSeconds == 0) {
            this.state = DISABLED;
        } else if (this.state == DISABLED) {
            this.state = OFF;
        }
    }

    public int state() {
        return state;
    }

    public boolean forced() {
        return forced;
    }

    /** {@code ForceScreenSaver}: the one state change this server has, since it does not blank. */
    public void setActive(boolean active) {
        this.state = active ? ON : (timeoutSeconds == 0 ? DISABLED : OFF);
        this.forced = true;
    }

    /** A client asked for the saver to be suspended - a video player's "do not blank" - which is kept. */
    private boolean suspended;

    public boolean suspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    // ------------------------------------------------------------------
    // DPMS
    // ------------------------------------------------------------------

    public int standbySeconds() {
        return standbySeconds;
    }

    public int suspendSeconds() {
        return suspendSeconds;
    }

    public int offSeconds() {
        return offSeconds;
    }

    public void setDpmsTimeouts(int standbySeconds, int suspendSeconds, int offSeconds) {
        this.standbySeconds = standbySeconds;
        this.suspendSeconds = suspendSeconds;
        this.offSeconds = offSeconds;
    }

    public boolean dpmsEnabled() {
        return dpmsEnabled;
    }

    public void setDpmsEnabled(boolean dpmsEnabled) {
        this.dpmsEnabled = dpmsEnabled;
    }

    public int powerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }
}
