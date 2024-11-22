package cn.oyzh.fx.plus.drag;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * 拖动增强
 *
 * @author oyzh
 * @since 2023/5/14
 */
@Accessors(chain = true, fluent = true)
public class DragNodeHandler {

    /**
     * 来源
     */
    @Setter
    @Getter
    private DragNodeItem source;

    /**
     * 目标
     */
    @Setter
    @Getter
    private DragNodeItem target;

    /**
     * 清理
     */
    public void clear() {
        this.clearDrapEffect();
        this.source = null;
        this.target = null;
    }

    /**
     * 清理拖动特效
     */
    public void clearDrapEffect() {
        if (this.source != null) {
            this.source.clearEffect();
        }
    }

    /**
     * 清理投放特效
     */
    public void clearDropEffect() {
        if (this.target != null) {
            this.target.clearEffect();
        }
    }

    /**
     * 初始化来源拖动特效
     *
     * @param source 节点
     */
    public void initDrapEffect(DragNodeItem source) {
        this.clearDrapEffect();
        source.initDragEffect();
        this.source = source;
    }

    /**
     * 初始化来源拖动特效
     *
     * @param target 节点
     */
    public void initDropEffect(DragNodeItem target) {
        this.clearDropEffect();
        target.initDropEffect();
        this.target = target;
    }

    /**
     * 执行投放
     */
    public void dropNode() {
        try {
            if (this.source != null && this.target != null) {
                this.target.onDropNode(this.source);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
