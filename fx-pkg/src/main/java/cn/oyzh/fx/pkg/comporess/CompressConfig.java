package cn.oyzh.fx.pkg.comporess;

import lombok.Data;

/**
 * 压缩配置
 *
 * @author oyzh
 * @since 2024/06/18
 */
@Data
public class CompressConfig {

    /**
     * 压缩类型
     */
    private String type;

    /**
     * 压缩文件名
     */
    private String name;

}
