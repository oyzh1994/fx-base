package cn.oyzh.fx.pkg.clip.filter;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * jar过滤器
 *
 * @author oyzh
 * @since 2022/12/13
 */
@Slf4j
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
        for (String excludeJar : this.getExcludes()) {
            if (name.toLowerCase().contains(excludeJar.toLowerCase())) {
                log.warn("JarFilter filter by excludes contains.");
                return true;
            }
        }
        return false;
    }
}
