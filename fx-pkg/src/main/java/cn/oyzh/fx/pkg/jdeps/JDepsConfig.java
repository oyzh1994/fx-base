package cn.oyzh.fx.pkg.jdeps;

import lombok.Data;

import java.util.Set;

/**
 * jar配置
 *
 * @author oyzh
 * @since 2024/06/17
 */
@Data
public class JDepsConfig {

    /**
     * 简要模式
     */
    private boolean summary = true;

    /**
     * 详细模式
     */
    private boolean verbose;

    /**
     * 跳过的文件
     */
    private Set<String> skips;

    /**
     * 排除的文件
     */
    private Set<String> excludes;

    /**
     * 多版本
     */
    private Integer multiRelease;

}
