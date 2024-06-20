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
     * 跳过的文件
     */
    private Set<String> skips;
}
