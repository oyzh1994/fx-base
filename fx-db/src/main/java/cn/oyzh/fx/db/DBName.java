package cn.oyzh.fx.db;

import cn.oyzh.common.util.StringUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 命名接口，提供名称的读写及新对象判断能力
 *
 * @author oyzh
 * @since 2026-09-02
 */
public interface DBName {

    /**
     * 获取名称
     *
     * @return 结果
     */
    String getName();

    /**
     * 设置名称
     *
     * @param name 名称
     */
    void setName(String name);

    /**
     * 是否新数据
     *
     * @return 结果
     */
    default boolean isNew() {
        return StringUtil.isBlank(this.getName());
    }

    /**
     * 对内容进行排序
     *
     * @param list 内容
     */
    static void sortItems(List<? extends DBName> list) {
        if (list != null) {
            list.sort((Comparator<DBName>) (o1, o2) -> {
                if (Objects.equals(o1.getName(),o2.getName())) {
                    return 0;
                }
                if (o1.getName() == null) {
                    return -1;
                }
                if (o2.getName() == null) {
                    return -1;
                }
                return o1.getName().compareTo(o2.getName());
            });
        }
    }
}
