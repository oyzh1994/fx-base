package cn.oyzh.fx.plus.window;

/**
 * 舞台监听接口
 *
 * @author oyzh
 * @since 2023/10/12
 */
public interface StageListener extends WindowListener {

    /**
     * 舞台初始化事件
     *
     * @param stage 舞台扩展
     */
    void onStageInitialize(StageAdapter stage);

    /**
     * 系统退出事件
     */
    void onSystemExit();
}
