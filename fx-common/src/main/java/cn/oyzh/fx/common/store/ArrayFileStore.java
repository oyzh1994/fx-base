package cn.oyzh.fx.common.store;

import cn.oyzh.fx.common.dto.Paging;
import cn.oyzh.fx.common.util.CollectionUtil;
import cn.oyzh.fx.common.util.ObjectComparator;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数组文件保存
 *
 * @author oyzh
 * @since 2023/10/26
 */
public abstract class ArrayFileStore<T extends ObjectComparator<T>> extends FileStore<T> {

    /**
     * 加载数据
     *
     * @return 数据列表
     */
    public abstract List<T> load();

    /**
     * 保存数据
     *
     * @param list 数据列表
     * @return 结果
     */
    public synchronized boolean save(List<T> list) {
        return this.saveData(list);
    }

    /**
     * 保存数据
     *
     * @param obj 数据
     * @return 结果
     */
    public synchronized boolean save(T obj) {
        return this.saveData(obj == null ? null : List.of(obj));
    }

    /**
     * 添加数据
     *
     * @param t 数据
     * @return 结果
     */
    public synchronized boolean add(@NonNull T t) {
        // 加载数据
        List<T> list = this.load();
        if (list == null) {
            list = new ArrayList<>();
            list.add(t);
            return this.saveData(list);
        }
        for (T t1 : list) {
            if (t1.compare(t)) {
                return true;
            }
        }
        list.add(t);
        return this.saveData(list);
    }

    /**
     * 修改数据
     *
     * @param t 数据
     * @return 结果
     */
    public synchronized boolean update(@NonNull T t) {
        // 加载数据
        List<T> list = this.load();
        if (CollectionUtil.isNotEmpty(list)) {
            for (T t1 : list) {
                if (t1.compare(t)) {
                    return this.saveData(list);
                }
            }
        }
        return false;
    }

    /**
     * 删除数据
     *
     * @param t 数据
     * @return 结果
     */
    public synchronized boolean delete(@NonNull T t) {
        // 加载数据
        List<T> list = this.load();
        if (CollectionUtil.isNotEmpty(list)) {
            if (list.removeIf(h -> h.compare(t))) {
                return this.saveData(list);
            }
        }

        return false;
    }

    /**
     * 查询，并转换为分页对象
     *
     * @param limit  每页数量
     * @param params 查询参数
     * @return 分页数据
     */
    public synchronized Paging<T> getPage(int limit, Map<String, Object> params) {
        // 加载数据
        List<T> list = this.load();
        // 分页对象
        Paging<T> paging = new Paging<>(list, limit);
        // 数据为空
        if (CollectionUtil.isEmpty(list)) {
            paging.dataList(new ArrayList<>());
        }
        return paging;
    }
}
