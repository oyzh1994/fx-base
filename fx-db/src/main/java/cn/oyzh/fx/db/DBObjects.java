package cn.oyzh.fx.db;

import java.util.Collection;
import java.util.List;

/**
 * 数据库对象集合
 *
 * @author oyzh
 * @since 2026-09-04
 */
public class DBObjects<E extends DBObject> extends DBObjectList<E> {

    /**
     * 构造数据库对象集合对象。
     */
    public DBObjects() {

    }

    /**
     * 构造对象集合
     *
     * @param list 对象集合
     */
    public DBObjects(Collection<E> list) {
        super.addAll(list);
    }

    /**
     * 创建对象集合
     *
     * @param list 对象列表
     * @param <T>  对象类型
     * @return 对象集合
     */
    public static <T extends DBObject> DBObjects<T> of(List<T> list) {
        return new DBObjects<>(list);
    }
}