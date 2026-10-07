package cn.oyzh.fx.plus.opacity;




/**
 * 透明度配置，包含窗口透明度与标题栏透明度
 *
 * @author oyzh
 * @since 2024/12/30
 */
public class OpacityConfig {

    /**
     * 窗口透明度
     */
    private Float windowOpacity;

    /**
     * 标题栏透明度
     */
    private Float titleOpacity;

    /**
     * 获取窗口不透明度。
     *
     * @return 窗口不透明度
     */
    public Float getWindowOpacity() {
        return windowOpacity;
    }

    /**
     * 设置窗口不透明度。
     *
     * @param windowOpacity 窗口不透明度
     */
    public void setWindowOpacity(Float windowOpacity) {
        this.windowOpacity = windowOpacity;
    }

    /**
     * 获取标题不透明度。
     *
     * @return 标题不透明度
     */
    public Float getTitleOpacity() {
        return titleOpacity;
    }

    /**
     * 设置标题不透明度。
     *
     * @param titleOpacity 标题不透明度
     */
    public void setTitleOpacity(Float titleOpacity) {
        this.titleOpacity = titleOpacity;
    }
}
