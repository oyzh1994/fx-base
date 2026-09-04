package cn.oyzh.fx.db;

import java.util.Collection;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/07/13
 */
public class DBObjects<E extends DBObject> extends DBObjectList<E> {

    public DBObjects() {

    }

    public DBObjects(Collection<E> list) {
        super.addAll(list);
    }
}