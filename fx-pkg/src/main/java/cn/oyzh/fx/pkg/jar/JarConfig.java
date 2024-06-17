package cn.oyzh.fx.pkg.jar;

import lombok.Data;

import java.util.Set;

/**
 * jar配置
 *
 * @author oyzh
 * @since 2024/06/17
 */
@Data
public class JarConfig {

    /**
     * 是否移除空jar
     */
    private boolean removeEmpty = true;

    /**
     * 排除的文件
     */
    private Set<String> excludes;
}
