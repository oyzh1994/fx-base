package cn.oyzh.fx.pkg.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则过滤器
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class RegFilter implements Function<String, Boolean> {

    /**
     * 排除的文件列表
     */
    private final Set<String> excludes = new CopyOnWriteArraySet<>();

    public RegFilter() {

    }

    public RegFilter(Collection<String> excludes) {
        this.addExcludes(excludes);
    }

    /**
     * 添加排除
     *
     * @param exclude 排除参数
     */
    public void addExclude(@NonNull String exclude) {
        if (StrUtil.isNotBlank(exclude)) {
            this.excludes.add(exclude.trim());
        }
    }

    /**
     * 添加排除集合
     *
     * @param collection 排除集合
     */
    public void addExcludes(Collection<String> collection) {
        if (CollUtil.isEmpty(collection)) {
            return;
        }
        for (String s : collection) {
            this.excludes.add(s.trim());
        }
    }

    @Override
    public Boolean apply(String fullName) {
        String name;
        // 对路径进行截取
        if (fullName.contains("/")) {
            name = fullName.substring(fullName.lastIndexOf("/") + 1);
        } else {
            name = fullName;
        }
        // 截取的内容为空，则不处理
        if (name.isEmpty()) {
            return true;
        }
        // 排除的文件
        for (String exclude : this.excludes) {
            // 正则模式
            if (exclude.contains(".*")) {
                // 编译正则表达式
                Pattern pattern = Pattern.compile(exclude, Pattern.CASE_INSENSITIVE);
                // 创建匹配器
                Matcher matcher = pattern.matcher(name);
                // 判断是否匹配
                if (matcher.matches()) {
                    return false;
                }
            } else if (exclude.equals(name) || exclude.endsWith(name) || name.endsWith(exclude)) {// 普通模式
                return false;
            }
        }
        return true;
    }
}
