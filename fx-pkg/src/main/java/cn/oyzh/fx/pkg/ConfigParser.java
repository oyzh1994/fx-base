package cn.oyzh.fx.pkg;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public interface ConfigParser<C> {

    C parse(String configFile);
}
