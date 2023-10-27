package cn.oyzh.fx.common.store;

import lombok.NonNull;

/**
 * 对象文件保存
 *
 * @author oyzh
 * @since 2023/10/26
 */
public abstract class ObjectFileStore<T> extends FileStore<T> {

    /**
     * 加载数据
     *
     * @return 数据
     */
    public abstract T load();

    /**
     * 修改数据
     *
     * @param obj 数据
     * @return 结果
     */
    public synchronized boolean update(@NonNull T obj) {
        return this.saveData(obj);
    }
}
