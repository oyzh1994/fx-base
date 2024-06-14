package cn.oyzh.fx.pkg.jre;

import lombok.Data;

import java.util.List;

/**
 * jre裁剪配置
 *
 * @author oyzh
 * @since 2023/03/09
 */
@Data
public class JreConfig {

    private List<String> excludes;

}
