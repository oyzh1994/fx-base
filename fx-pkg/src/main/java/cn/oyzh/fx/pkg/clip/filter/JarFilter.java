package cn.oyzh.fx.pkg.clip.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;

/**
 * jar过滤器
 *
 * @author oyzh
 * @since 2022/12/13
 */
//@Slf4j
public class JarFilter extends BaseFilter {

    @Override
    public String parseName(String name) {
        // 非jar，不过滤
        if (StrUtil.isBlank(name) || !name.endsWith(".jar")) {
            return null;
        }
        return name;
    }

    @Override
    public boolean acceptExclude(String name) {
        // 过滤的jar
        for (String exclude : this.getExcludes()) {
            if (name.toLowerCase().contains(exclude.toLowerCase())) {
                StaticLog.warn("{} acceptExclude by exclude:{} contains.", name, exclude);
                return true;
            }
        }
        return false;
    }
}
