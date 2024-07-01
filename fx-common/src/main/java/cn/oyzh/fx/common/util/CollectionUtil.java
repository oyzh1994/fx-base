package cn.oyzh.fx.common.util;

import cn.hutool.core.collection.CollUtil;
import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * @author oyzh
 * @since 2024/7/1
 */
@UtilityClass
public class CollectionUtil {

    public static  <T> T get(Collection<T> collection, int index) {
        if (index < 0) {
            return null;
        }
        return CollUtil.get(collection, index);
    }
}
