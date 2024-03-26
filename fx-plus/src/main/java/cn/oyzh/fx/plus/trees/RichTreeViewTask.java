package cn.oyzh.fx.plus.trees;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 渲染任务
 *
 * @author oyzh
 * @since 2024/3/26
 */
@Data
@AllArgsConstructor
public class RichTreeViewTask {

    /**
     * 任务
     */
    private Runnable task;

    /**
     * 类型
     * 0: 默认线程
     * 1: fx线程
     * 2: fx线程，异步
     */
    private byte type;


}
