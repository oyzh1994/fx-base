package cn.oyzh.fx.plus.domain;

import lombok.Setter;

/**
 * 窗口信息
 *
 * @author oyzh
 * @since 2023/1/17
 * @see Setting
 */
@Deprecated
public class PageInfo {

    /**
     * 页面宽
     */
    @Setter
    private Double width;

    /**
     * 页面高
     */
    @Setter
    private Double height;

    /**
     * 屏幕x
     */
    @Setter
    private Double screenX;

    /**
     * 屏幕y
     */
    @Setter
    private Double screenY;

    /**
     * 是否最大化
     */
    @Setter
    private Boolean maximized;

    /**
     * 主页左侧宽
     */
    @Setter
    private Double mainLeftWidth;

    /**
     * 获取页面宽
     *
     * @return 页面宽
     */
    public Double getWidth() {
        return this.width == null ? null : this.width < 100 ? null : this.width;
    }

    /**
     * 获取页面高
     *
     * @return 页面高
     */
    public Double getHeight() {
        return this.height == null ? null : this.height < 100 ? null : this.height;
    }

    /**
     * 获取屏幕x
     *
     * @return 屏幕x
     */
    public Double getScreenX() {
        return this.screenX == null ? null : this.screenX;
    }

    /**
     * 获取屏幕y
     *
     * @return 屏幕y
     */
    public Double getScreenY() {
        return this.screenY == null ? null : this.screenY;
    }

    /**
     * 是否最大化
     *
     * @return 结果
     */
    public Boolean isMaximized() {
        return this.maximized != null && this.maximized;
    }

    /**
     * 获取主页左侧宽
     *
     * @return 主页左侧宽
     */
    public Double getMainLeftWidth() {
        return this.mainLeftWidth == null ? null : this.mainLeftWidth < 100 ? null : this.mainLeftWidth;
    }
}
