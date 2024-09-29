package cn.oyzh.fx.common.util;

import java.util.Collection;

/**
 * @author oyzh
 * @since 2024-09-26
 */
public class DestroyUtil {

    public static void destroy(Object obj) {
        if (obj instanceof Destroyable destroyable) {
            destroyable.destroy();
        }
    }

    public static void destroy(Collection<?> collection) {
        if(collection!=null && !collection.isEmpty()){
            for (Object object : collection) {
                destroy(object);
            }
        }
    }
}
