package cn.oyzh.fx.pkg;

/**
 * 配置解析器
 *
 * @author oyzh
 * @since 2024/6/14
 */
public interface ConfigParser<C> {

    /**
     * 解析配置
     *
     * @param configFile 配置文件
     * @return 配置类
     */
    C parse(String configFile);
}
