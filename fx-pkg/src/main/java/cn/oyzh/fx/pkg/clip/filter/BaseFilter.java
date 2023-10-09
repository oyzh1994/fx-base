package cn.oyzh.fx.pkg.clip.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 基础过滤器
 *
 * @author oyzh
 * @since 2022/12/13
 */
public abstract class BaseFilter {

    /**
     * 排除的文件列表
     */
    @Getter
    private final Set<String> excludes = new CopyOnWriteArraySet<>();

    /**
     * 解析名称
     *
     * @param name 名称
     * @return 解析后的名称
     */
    public String parseName(String name) {
        return name;
    }

    /**
     * 是否接受
     *
     * @param name 名称
     * @return 接受返回true
     */
    public boolean accept(String name) {
        name = this.parseName(name);
        return name == null || !this.acceptExclude(name);
    }

    /**
     * 是否排除
     *
     * @param name 名称
     * @return 排除返回true
     */
    public abstract boolean acceptExclude(String name);

    /**
     * 添加排除
     *
     * @param exclude 排除参数
     */
    public void addExclude(@NonNull String exclude) {
        if (this.getExcludes() == null) {
            return;
        }
        if (StrUtil.isNotBlank(exclude)) {
            this.getExcludes().add(exclude.trim());
        }
    }

    /**
     * 添加排除集合
     *
     * @param list 排除集合
     */
    public void addExcludes(Collection<String> list) {
        if (this.getExcludes() == null) {
            return;
        }
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (String s : list) {
            this.getExcludes().add(s.trim());
        }
    }
}
