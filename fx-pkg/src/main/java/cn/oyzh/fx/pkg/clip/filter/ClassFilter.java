package cn.oyzh.fx.pkg.clip.filter;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * class过滤器
 *
 * @author oyzh
 * @since 2022/12/13
 */
@Slf4j
public class ClassFilter extends BaseFilter {

    @Override
    public String parseName(String name) {
        // 非class，不过滤
        if (StrUtil.isBlank(name) || !name.endsWith(".class")) {
            return null;
        }

        // 处理class
        name = name.replace("BOOT-INF/classes/", "")
                .replace(".class", "")
                .replace("/", ".");
        return name;
    }

    @Override
    public boolean acceptExclude(String name) {
        // 排除的class
        if (this.getExcludes().contains(name)) {
            log.warn("ClassFilter filter by excludes contains.");
            return true;
        }

        // 排除的class子类或者包
        for (String exclude : this.getExcludes()) {
            if (name.startsWith(exclude)) {
                log.warn("ClassFilter unFilter by excludes:{} startsWith.", exclude);
                return true;
            }
        }
        return false;
    }
}
