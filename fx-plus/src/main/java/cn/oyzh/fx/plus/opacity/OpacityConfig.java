package cn.oyzh.fx.plus.opacity;




/**
 * 透明度配置
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

    public Float getWindowOpacity() {
        return windowOpacity;
    }

    public void setWindowOpacity(Float windowOpacity) {
        this.windowOpacity = windowOpacity;
    }

    public Float getTitleOpacity() {
        return titleOpacity;
    }

    public void setTitleOpacity(Float titleOpacity) {
        this.titleOpacity = titleOpacity;
    }
}
