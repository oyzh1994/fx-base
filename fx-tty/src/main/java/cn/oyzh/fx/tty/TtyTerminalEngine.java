package cn.oyzh.fx.tty;

import java.util.Locale;

/**
 * 终端渲染/模拟引擎。
 *
 * <p>fx-tty 同时保留两套终端实现：</p>
 * <ul>
 *     <li>{@link #JEDITERM}：原有 JediTerm 实现；</li>
 *     <li>{@link #GHOSTTY}：Ghostty 实现，使用 libghostty-vt 作为终端内核。</li>
 * </ul>
 *
 * <p>引擎可以在打开会话前通过 {@link TtyTermWidget#setEngine(TtyTerminalEngine)}
 * 单独指定，也可以通过 {@link #setDefaultEngine(TtyTerminalEngine)} 全局指定。
 * 也可以通过系统属性 {@code -Dfx.tty.engine=ghostty} 指定默认引擎。</p>
 *
 * @author oyzh
 * @since 2026-11-11
 */
public enum TtyTerminalEngine {

    /**
     * JediTerm 引擎（原有实现，可在设置中切回）
     */
    JEDITERM("jediterm"),

    /**
     * Ghostty 引擎
     */
    GHOSTTY("ghostty");

    /**
     * 默认引擎的系统属性名
     */
    public static final String PROPERTY = "fx.tty.engine";

    /**
     * 内置默认引擎；修改这里即可改变全局默认值
     */
    private static final TtyTerminalEngine BUILTIN_DEFAULT = JEDITERM;

    private static volatile TtyTerminalEngine defaultEngine = resolveDefault();

    private final String id;

    TtyTerminalEngine(String id) {
        this.id = id;
    }

    /**
     * 获取引擎标识。
     *
     * @return 引擎标识
     */
    public String id() {
        return this.id;
    }

    /**
     * 根据标识解析引擎。
     *
     * @param id 标识，支持 jediterm / ghostty，忽略大小写
     * @return 引擎，无法识别或为空时返回当前默认引擎
     */
    public static TtyTerminalEngine of(String id) {
        if (id != null) {
            String value = id.trim().toLowerCase(Locale.ROOT);
            for (TtyTerminalEngine engine : values()) {
                if (engine.id.equals(value)) {
                    return engine;
                }
            }
        }
        return defaultEngine;
    }

    /**
     * 获取全局默认引擎。
     *
     * @return 默认引擎
     */
    public static TtyTerminalEngine getDefaultEngine() {
        return defaultEngine;
    }

    /**
     * 设置全局默认引擎，只影响之后打开的终端会话。
     *
     * @param engine 引擎，null 时忽略
     */
    public static void setDefaultEngine(TtyTerminalEngine engine) {
        if (engine != null) {
            defaultEngine = engine;
        }
    }

    private static TtyTerminalEngine resolveDefault() {
        String value = System.getProperty(PROPERTY);
        if (value != null) {
            String id = value.trim().toLowerCase(Locale.ROOT);
            for (TtyTerminalEngine engine : values()) {
                if (engine.id.equals(id)) {
                    return engine;
                }
            }
        }
        return BUILTIN_DEFAULT;
    }
}
