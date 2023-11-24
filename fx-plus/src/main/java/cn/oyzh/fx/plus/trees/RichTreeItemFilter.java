package cn.oyzh.fx.plus.trees;

import java.util.function.Function;

/**
 * 富功能树节点过滤器
 *
 * @author oyzh
 * @since 2023/11/10
 */
public interface RichTreeItemFilter extends Function<RichTreeItem<?>, Boolean> {

}
