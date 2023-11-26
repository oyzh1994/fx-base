package cn.oyzh.fx.pkg.clip.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;

/**
 * class过滤器
 *
 * @author oyzh
 * @since 2022/12/13
 */
//@Slf4j
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
            StaticLog.warn("{} acceptExclude by excludes contains.", name);
            return true;
        }
        // 排除的class子类或者包
        for (String exclude : this.getExcludes()) {
            if (name.startsWith(exclude)) {
                StaticLog.warn("{} acceptExclude by exclude:{} startsWith.", name, exclude);
                return true;
            }
        }
        return false;
    }
}
